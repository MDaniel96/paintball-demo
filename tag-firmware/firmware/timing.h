#ifndef TIMING_H
#define TIMING_H

#include <stdint.h>

#define TIMING_DISCOVERY_SUPERFRAME_COUNT   5

#define TIMING_ANCHOR_COUNT             4


#define TIMING_ANCHOR_MESSAGE_TIMESLOT_US                       5000
#define TIMING_MESSAGE_TX_PREFIX_TIME_US                      (1051 + 400)          // RMARKER delay + DW1000 comm delay


#define TIMING_SUPERFRAME_LENGTH_US     (((TIMING_ANCHOR_COUNT) * 2 ) * TIMING_ANCHOR_MESSAGE_TIMESLOT_US)
#define TIMING_SUPERFRAME_LENGTH_MS     (TIMING_SUPERFRAME_LENGTH_US/1000)

#define SPEED_OF_LIGHT					299702547

#define ANT_DLY_CH1                                             16414
#define TX_ANT_DLY                                              ANT_DLY_CH1             //16620 //16436 /* Default antenna delay values for 64 MHz PRF */
#define RX_ANT_DLY                                              ANT_DLY_CH1

#define CORRECT_CLOCK_DIFF(x)   x = ((x) < 0)?((x) + (1ll << 40)):(x)



#endif // TIMING_H
