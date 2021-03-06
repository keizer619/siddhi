/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.query.pattern;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class ComplexPatternTestCase {

    private static final Logger log = Logger.getLogger(ComplexPatternTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("testPatternComplex1 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every ( e1=Stream1[price > 20] -> e2=Stream2[price > e1.price] or e3=Stream2['IBM' == symbol]) " +
                "  -> e4=Stream2[price > e1.price] " +
                "select e1.price as price1, e2.price as price2, e3.price as price3, e4.price as price4 " +
                "insert into OutputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{55.6f, 55.7f, null, 57.7f}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{54.0f, 57.7f, null, 59.7f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = executionPlanRuntime.getInputHandler("Stream1");
        InputHandler stream2 = executionPlanRuntime.getInputHandler("Stream2");

        executionPlanRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 55f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 54f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 57.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 59.7f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("testPatternComplex2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every ( e1=Stream1[price > 20] -> e2=Stream1[price > 20]<1:2>) -> e3=Stream1[price > e1.price] " +
                "select e1.price as price1, e2[0].price as price2_0, e2[1].price as price2_1, e3.price as price3 " +
                "insert into OutputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{55.6f, 54.0f, 53.6f, 57.0f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = executionPlanRuntime.getInputHandler("Stream1");
        InputHandler stream2 = executionPlanRuntime.getInputHandler("Stream2");

        executionPlanRuntime.start();

        stream1.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 54f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 53.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 57f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }


    @Test
    public void testQuery3() throws InterruptedException {
        log.info("testPatternComplex3 - OUT 3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1 = Stream1 [ price >= 50 and volume > 100 ] -> e2 = Stream1 [price <= 40 ] <2:> -> e3 = Stream1 [volume <= 70 ] " +
                "select e1.symbol as symbol1, e2[last].symbol as symbol2, e3.symbol as symbol3 " +
                "insert into StockQuote;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{"IBM", "FB", "WSO2"}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{"ADP", "WSO2", "AMZN"}, event.getData());
                                break;
                            case 3:
                                Assert.assertArrayEquals(new Object[]{"WSO2", "QQQ", "CSCO"}, event.getData());
                                break;
                            default:
                                Assert.assertSame(3, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = executionPlanRuntime.getInputHandler("Stream1");

        executionPlanRuntime.start();

        stream1.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        stream1.send(new Object[]{"GOOG", 39.8f, 91});
        stream1.send(new Object[]{"FB", 35f, 81});
        stream1.send(new Object[]{"WSO2", 21f, 61});
        stream1.send(new Object[]{"ADP", 50f, 101});
        stream1.send(new Object[]{"GOOG", 41.2f, 90});
        stream1.send(new Object[]{"FB", 40f, 100});
        stream1.send(new Object[]{"WSO2", 33.6f, 85});
        stream1.send(new Object[]{"AMZN", 23.5f, 55});
        stream1.send(new Object[]{"WSO2", 51.7f, 180});
        stream1.send(new Object[]{"TXN", 34f, 61});
        stream1.send(new Object[]{"QQQ", 24.6f, 45});
        stream1.send(new Object[]{"CSCO", 181.6f, 40});
        stream1.send(new Object[]{"WSO2", 53.7f, 200});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 3, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery4() throws InterruptedException {
        log.info("testPatternComplex4 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1 = Stream1 [ price >= 50 and volume > 100 ] " +
                "   -> e2 = Stream2 [price <= 40 ] <1:> -> e3 = Stream2 [volume <= 70 ] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as symbol3 " +
                "insert into StockQuote;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{"WSO2", "DDD", 60}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = executionPlanRuntime.getInputHandler("Stream1");
        InputHandler stream2 = executionPlanRuntime.getInputHandler("Stream2");

        executionPlanRuntime.start();

        stream1.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 21f, 81});
        stream2.send(new Object[]{"WSO2", 176.6f, 65});
        stream1.send(new Object[]{"BIRT", 21f, 81});
        stream1.send(new Object[]{"AMBA", 126.6f, 165});
        stream2.send(new Object[]{"DDD", 23f, 181});
        stream2.send(new Object[]{"BIRT", 21f, 86});
        stream2.send(new Object[]{"BIRT", 21f, 82});
        stream2.send(new Object[]{"WSO2", 176.6f, 60});
        stream1.send(new Object[]{"AMBA", 126.6f, 165});
        stream2.send(new Object[]{"DOX", 16.2f, 25});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery5() throws InterruptedException {
        log.info("testPatternComplex5 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1 = Stream1 [ price >= 50 and volume > 100 ] -> e2 = Stream2 [e1.symbol != 'AMBA' ] " +
                "   -> e3 = Stream2 [volume <= 70 ] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as volume3 " +
                "insert into StockQuote;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = executionPlanRuntime.getInputHandler("Stream1");
        InputHandler stream2 = executionPlanRuntime.getInputHandler("Stream2");

        executionPlanRuntime.start();

        stream1.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 21f, 81});
        stream2.send(new Object[]{"WSO2", 176.6f, 65});
        stream1.send(new Object[]{"BIRT", 21f, 81});
        stream1.send(new Object[]{"AMBA", 126.6f, 165});
        stream2.send(new Object[]{"DDD", 23f, 181});
        stream2.send(new Object[]{"BIRT", 21f, 86});
        stream2.send(new Object[]{"BIRT", 21f, 82});
        stream2.send(new Object[]{"WSO2", 176.6f, 60});
        stream1.send(new Object[]{"AMBA", 126.6f, 165});
        stream2.send(new Object[]{"DOX", 16.2f, 25});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery6() throws InterruptedException {
        log.info("testPatternComplex6 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1 = Stream1 -> e2 = Stream2 [e1.symbol != 'AMBA' ] <2:> -> e3 = Stream2 [volume <= 70 ] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as volume3 " +
                "insert into StockQuote;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{"IBN", "DDD", 70}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stream1 = executionPlanRuntime.getInputHandler("Stream1");
        InputHandler stream2 = executionPlanRuntime.getInputHandler("Stream2");

        executionPlanRuntime.start();

        stream1.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        stream2.send(new Object[]{"GOOG", 21f, 51});
        stream2.send(new Object[]{"FBX", 21f, 81});
        stream2.send(new Object[]{"WSO2", 176.6f, 65});
        stream1.send(new Object[]{"BIRT", 21f, 81});
        stream1.send(new Object[]{"AMBA", 126.6f, 165});
        stream2.send(new Object[]{"DDD", 23f, 181});
        stream2.send(new Object[]{"BIRT", 21f, 86});
        stream2.send(new Object[]{"IBN", 21f, 70});
        stream2.send(new Object[]{"WSO2", 176.6f, 90});
        stream1.send(new Object[]{"AMBA", 126.6f, 165});
        stream2.send(new Object[]{"DOX", 16.2f, 25});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

}
