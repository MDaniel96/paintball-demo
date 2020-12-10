#include "impl_tag.h"
#include <stdint.h>

#include "app_scheduler.h"
#include "dwm_utils.h"
#include "log.h"
#include "utils.h"
#include "address_handler.h"
#include "nrf_gpio.h"
#include "nrf_drv_timer.h"
#include "timing.h"
#include "accel_service.h"
#include "ranging_service.h"
#include "accel.h"
#include "data_format.h"

#define TAG "tag"

#define SYNC_COMPENSATION_CONST_US	150

typedef enum {
	TAG_STATE__DISCOVERY = 0,
	TAG_STATE__SYNCHRONIZED = 1
} tag_state_t;

typedef enum
{
	EVENT_RX = 1,
	EVENT_TIMER = 2,
	EVENT_SF_BEGIN = 3
} event_type_t;

const static nrf_drv_timer_t	m_frame_timer = NRF_DRV_TIMER_INSTANCE(1);
static int32_t					m_frame_timer_compensation_us = 0;
static tag_state_t				m_tag_state = TAG_STATE__DISCOVERY;
static uint16_t					m_tag_id = 0;
static uint16_t                 m_superframe_ts=0;
static uint8_t                  m_superframe_id=0;
static uint32_t                 m_superframe_counter = 0;
static uint8_t					m_rx_buffer[SF_MAX_MESSAGE_SIZE];
static uint8_t					m_received_sync_messages_count = 0;
static uint8_t					m_unsynced_sf_count = 0;
static uint8_t                  m_tag_mode = TAG_MODE_POWERDOWN;
static tag_to_ble_msg_t         ble_msg;

static void frame_timer_event_handler(nrf_timer_event_t event_type, void* p_context);
static void restart_frame_timer();
static void set_frame_timer(uint32_t us);
static void event_handler(event_type_t event_type, const uint8_t* data, uint16_t datalength);


static void set_tag_state(tag_state_t newstate)
{
	m_tag_state = newstate;
}

inline static uint32_t get_slot_time_by_message(dwm1000_ts_t rx_ts)
{
    uint32_t rx_ts_32 = rx_ts.ts >> 8;
    uint32_t systime_32 = dwm1000_get_system_time_u64().ts >> 8;

	return (systime_32-rx_ts_32)/(UUS_TO_DWT_TIME/256) + TIMING_MESSAGE_TX_PREFIX_TIME_US;
}

static void mac_rxok_callback_impl(const dwt_cb_data_t *data)
{
	if(data->datalength - 2 > SF_MAX_MESSAGE_SIZE)
	{
		LOGE(TAG,"oversized msg\n");
		dwt_rxenable(0);
		return;
	}

	dwt_readrxdata(m_rx_buffer, data->datalength - 2, 0);

	event_handler(EVENT_RX, m_rx_buffer, data->datalength - 2);

	dwt_rxenable(0);
}

static void mac_rxerr_callback_impl(const dwt_cb_data_t *data)
{
	LOGI(TAG, "rx err\n");

	dwt_rxenable(0);
}

static void mac_txok_callback_impl(const dwt_cb_data_t* data)
{
	dwt_rxenable(0);
}

static void restart_frame_timer() {
	m_frame_timer_compensation_us = 0;

	nrf_drv_timer_pause(&m_frame_timer);
	nrf_drv_timer_clear(&m_frame_timer);
	uint32_t time_ticks = nrf_drv_timer_us_to_ticks(&m_frame_timer, TIMING_SUPERFRAME_LENGTH_US);
	nrf_drv_timer_compare(&m_frame_timer, NRF_TIMER_CC_CHANNEL1, time_ticks, true);
	nrf_drv_timer_resume(&m_frame_timer);
}

static void compensate_frame_timer(uint32_t c)
{
	uint32_t state = nrf_drv_timer_capture(&m_frame_timer, NRF_TIMER_CC_CHANNEL2) >> 4;
	LOGT(TAG,"state: %ld\n", state);

	m_frame_timer_compensation_us = c - state;

	nrf_drv_timer_pause(&m_frame_timer);
	uint32_t time_ticks = nrf_drv_timer_us_to_ticks(&m_frame_timer, TIMING_SUPERFRAME_LENGTH_US - m_frame_timer_compensation_us);
	nrf_drv_timer_compare(&m_frame_timer, NRF_TIMER_CC_CHANNEL1, time_ticks, true);
	nrf_drv_timer_resume(&m_frame_timer);
}

static void tag_ranging_sched_handler(void *p_event_data, uint16_t event_size)
{
   // LOGI(TAG, "sending ranging (%d)\n", event_size);

    ble_rs_send((tag_to_ble_msg_t*) p_event_data, sizeof(tag_to_ble_msg_t));

}

static void acc_measurement_sched_handler(void * p_event_data, uint16_t event_size)
{
    df_accel_info_t* event = p_event_data;

    LOGT(TAG,"Ts: %" PRIu16 "\n", event->ts);
    for (uint8_t i = 0; i < ACCEL_FIFO_BURST_SIZE; i++)
    {
        int16_t x = (event->values[i].x);
        int16_t y = (event->values[i].y);
        int16_t z = (event->values[i].z);

        LOGT(TAG,"Accel: %" PRIi16 ", %" PRIi16 ", %" PRIi16 "\n", x, y, z);
    }

    ble_accs_send_values((df_accel_info_t*)p_event_data);
}


static void event_handler(event_type_t event_type, const uint8_t* data, uint16_t datalength)
{
	LOGT(TAG, "S %d, E %d (%ld)\n", m_tag_state, event_type, (nrf_drv_timer_capture(&m_frame_timer, NRF_TIMER_CC_CHANNEL2) >> 4));

	if(event_type == EVENT_SF_BEGIN)
	{

	    m_superframe_ts = (uint16_t)utils_get_tick_time();

		restart_frame_timer();
		LOGT(TAG, "SF\n");
        m_superframe_counter++;

        if(m_received_sync_messages_count == 0)
        {
            m_unsynced_sf_count++;
            LOGW(TAG, "sync warn\n");

            if (m_unsynced_sf_count > 5)
            {
                set_tag_state(TAG_STATE__DISCOVERY);
                LOGE(TAG, "sync lost\n");

                m_superframe_counter = 0;

                return;
            }
        }

		m_received_sync_messages_count = 0;

	}

	if(m_tag_state == TAG_STATE__SYNCHRONIZED)
	{
		if(event_type == EVENT_RX)
		{
		    sf_anchor_msg_t* msg = (sf_anchor_msg_t*)data;

			dwm1000_ts_t rx_ts = dwm1000_get_rx_timestamp_u64();
			uint32_t slot_time_us = get_slot_time_by_message(rx_ts);
			uint32_t sf_time_us = (2 * msg->hdr.src_id + 1) * TIMING_ANCHOR_MESSAGE_TIMESLOT_US + slot_time_us;

			compensate_frame_timer(sf_time_us);

			LOGT(TAG,"SFT %ld\n", sf_time_us)

			m_superframe_id = msg->tr_id;

			m_received_sync_messages_count++;
			m_unsynced_sf_count = 0;

            if(m_tag_mode == TAG_MODE_TAG_RANGING)
            {
                ble_msg = put_anchor_msg_to_ble(rx_ts, msg);
                app_sched_event_put(&ble_msg, sizeof(tag_to_ble_msg_t), tag_ranging_sched_handler);
            }
		}
	}

    else if(m_tag_state == TAG_STATE__DISCOVERY)
    {
        if(event_type == EVENT_RX)
        {
            // Found anchor message, sync

            sf_anchor_msg_t* msg = (sf_anchor_msg_t*)data;

            set_tag_state(TAG_STATE__SYNCHRONIZED);

            uint32_t slot_time_us = get_slot_time_by_message(dwm1000_get_rx_timestamp_u64());
            uint32_t sf_time_us = (2 * msg->hdr.src_id + 1)* TIMING_ANCHOR_MESSAGE_TIMESLOT_US + slot_time_us;

            compensate_frame_timer(sf_time_us);

            LOGT(TAG,"SFT %ld\n", sf_time_us)

            m_superframe_id = msg->tr_id;

        }
    }
}

static void frame_timer_event_handler(nrf_timer_event_t event_type, void* p_context)
{
	if(event_type == NRF_TIMER_EVENT_COMPARE0)
		event_handler(EVENT_TIMER, NULL, 0);
	else if(event_type == NRF_TIMER_EVENT_COMPARE1)
		event_handler(EVENT_SF_BEGIN, NULL, 0);
}

static void start_uwb_comm() {
    LOGI(TAG,"initialize dw1000 phy\n");
    dwm1000_phy_init();
    dwm1000_irq_enable();

    dwt_setcallbacks(mac_txok_callback_impl, mac_rxok_callback_impl, NULL, mac_rxerr_callback_impl);

    uint32_t err_code;
    nrf_drv_timer_config_t timer_cfg = {
        .frequency          = NRF_TIMER_FREQ_16MHz,
        .mode               = NRF_TIMER_MODE_TIMER,
        .bit_width          = NRF_TIMER_BIT_WIDTH_32,
        .interrupt_priority = 2,
        .p_context          = NULL
    };
    err_code = nrf_drv_timer_init(&m_frame_timer, &timer_cfg, frame_timer_event_handler);
    APP_ERROR_CHECK(err_code);
    nrf_drv_timer_enable(&m_frame_timer);
    nrf_drv_timer_pause(&m_frame_timer);

    set_tag_state(TAG_STATE__DISCOVERY);

    restart_frame_timer();

    dwt_rxenable(0);
}

static void stop_uwb_comm() {
    LOGI(TAG,"deinit dw1000 phy\n");

    dwt_forcetrxoff();

    nrf_drv_timer_disable(&m_frame_timer);
    nrf_drv_timer_uninit(&m_frame_timer);

    dwm1000_irq_disable();
    dwm1000_phy_release();
}

static void ble_accs_status_callback(df_accel_mode_t mode)
{
	LOGI(TAG,"accel status: %02X\n", *((uint8_t*)&mode));

	switch(mode.mode)
	{
	case ACCS_MODE_POWERDOWN:
		accel_state(LIS2DH12_ODR_POWERDOWN, mode.hpf_enabled);
		break;
	case ACCS_MODE_1HZ:
		accel_state(LIS2DH12_ODR_1HZ, mode.hpf_enabled);
		break;
	case ACCS_MODE_10HZ:
		accel_state(LIS2DH12_ODR_10HZ, mode.hpf_enabled);
		break;
	case ACCS_MODE_25HZ:
		accel_state(LIS2DH12_ODR_25HZ, mode.hpf_enabled);
		break;
	case ACCS_MODE_50HZ:
		accel_state(LIS2DH12_ODR_50HZ, mode.hpf_enabled);
		break;
	}
}

static void ble_rs_mode_callback(tag_mode_t mode)
{
    LOGI(TAG,"tag mode: 0x%02X\n", mode);

    if(mode == m_tag_mode)
    {
        return;
    }

    if(m_tag_mode == TAG_MODE_POWERDOWN &&
            (mode == TAG_MODE_TAG_RANGING ))
    {
        start_uwb_comm();
		utils_use_tick_timer();
    }

    if((m_tag_mode == TAG_MODE_TAG_RANGING) &&
            mode == TAG_MODE_POWERDOWN)
    {
        stop_uwb_comm();
		utils_release_tick_timer();
    }

    m_tag_mode = mode;
}


int impl_tag_init()
{
	m_tag_id = addr_handler_get_virtual_addr();
	if(m_tag_id == 0xFFFF)
	{
		LOGE(TAG, "no address specified\n");
		for(;;);
	}

//	if(m_tag_id >= TIMING_TAG_COUNT)
//	{
//        ERROR(TAG, "tag count reached\n");
//	}

	LOGI(TAG,"mode: tag\n");
	LOGI(TAG,"addr: %04X\n", m_tag_id);
	LOGI(TAG,"sf length: %d\n", TIMING_SUPERFRAME_LENGTH_MS);

    //NRF_CLOCK->EVENTS_HFCLKSTARTED = 0;
    //NRF_CLOCK->TASKS_HFCLKSTART = 1;

//    ranging_init(m_tag_id);
//    ranging_anchor_init();

	//uart_init();

    accel_init(acc_measurement_sched_handler);

    ble_accs_set_status_callback(ble_accs_status_callback);
    ble_rs_set_tag_mode_callback(ble_rs_mode_callback);

	return 0;
}



