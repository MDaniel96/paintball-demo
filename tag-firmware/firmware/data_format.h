#ifndef DATA_FORMAT_H
#define DATA_FORMAT_H

#include <stdint.h>
#include "timing.h"
#include "lis2dh12.h"



#define ACCEL_FIFO_BURST_SIZE             10

typedef struct {
    uint16_t                ts;
    lis2dh12_data_t         values[ACCEL_FIFO_BURST_SIZE];
} __attribute__((packed)) df_accel_info_t;


typedef struct {
    uint16_t                group_id;
    uint16_t                dev_id;

    uint16_t                 anchor_count;
    uint16_t                 tag_count;

    uint32_t                failed_notification_count;
    uint32_t                notification_count;
} __attribute__((packed)) df_device_info_t;


//message types
#define SF_HEADER_FCTRL_MSG_TYPE_ANCHOR_MESSAGE    0x01
#define SF_HEADER_FCTRL_MSG_TYPE_TAG_MESSAGE       0x02

typedef struct
{
    //uint8_t          fctrl;
    uint16_t         group_id;
    uint16_t         src_id;
} __attribute__((packed)) sf_header_t;

typedef struct {
    uint8_t          rx_ts[5];
} __attribute__((packed)) rx_info_t;

typedef struct {
    uint8_t         src_id;
    rx_info_t       rx_ts[2];

} __attribute__((packed)) anc_msg_rx_tss_t;

typedef struct {
    sf_header_t         hdr;
    uint8_t             tr_id;
    uint8_t             tx_ts[5];
    uint8_t             parity;

    anc_msg_rx_tss_t    anchors[2];

} __attribute__((packed)) sf_anchor_msg_t;

typedef struct {
    sf_anchor_msg_t    anc_msg;
   rx_info_t      tag_rx_ts;
} __attribute__((packed)) tag_to_ble_msg_t;

#define SF_MAX_MESSAGE_SIZE    (sizeof(sf_anchor_msg_t))

#endif // DATA_FORMAT_H
