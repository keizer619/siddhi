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

package org.wso2.siddhi.core.query.sequence;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

/**
 * Created on 12/15/14.
 */
public class SequenceTestCase {

    private static final Logger log = Logger.getLogger(SequenceTestCase.class);
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
        log.info("testSequence1 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from e1=Stream1[price>20],e2=Stream2[price>e1.price] " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    Assert.assertArrayEquals(new Object[]{"WSO2", "IBM"}, inEvents[0].getData());
                    eventArrived = true;
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
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("testSequence2 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], e2=Stream2[price>e1.price] " +
                "select e1.symbol as symbol1, e2.symbol as symbol2 " +
                "insert into OutputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    Assert.assertArrayEquals(new Object[]{"GOOG", "IBM"}, inEvents[0].getData());
                    eventArrived = true;
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
        stream1.send(new Object[]{"GOOG", 57.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 65.7f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }


    @Test
    public void testQuery3() throws InterruptedException {
        log.info("testSequence3 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], e2=Stream2[price>e1.price]* " +
                "select e1.symbol as symbol1, e2[0].symbol as symbol2, e2[1].symbol as symbol3 " +
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
                                Assert.assertArrayEquals(new Object[]{"WSO2", null, null}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{"IBM", null, null}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
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
        stream1.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery4() throws InterruptedException {
        log.info("testSequence4 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20]*, e2=Stream1[price>e1[0].price] " +
                "select e1[0].price as price1, e1[1].price as price2, e2.price as price3 " +
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
                                Assert.assertArrayEquals(new Object[]{55.6f, 55.7f, 57.6f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }


    @Test
    public void testQuery5() throws InterruptedException {
        log.info("testSequence5 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20]*, e2=Stream1[price>e1[0].price] " +
                "select e1[0].price as price1, e1[1].price as price2, e2.price as price3 " +
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
                                Assert.assertArrayEquals(new Object[]{55.6f, 55.0f, 57.6f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery6() throws InterruptedException {
        log.info("testSequence6 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20]?, e2=Stream1[price>e1[0].price] " +
                "select e1[0].price as price1, e2.price as price3 " +
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
                                Assert.assertArrayEquals(new Object[]{55.7f, 57.6f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery7() throws InterruptedException {
        log.info("testSequence7 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20], e2=Stream2[price>e1.price] or e3=Stream2[symbol=='IBM'] " +
                "select e1.price as price1, e2.price as price2, e3.price as price3 " +
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
                                Assert.assertArrayEquals(new Object[]{55.6f, 55.7f, null}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{55.7f, 57.6f, null}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream2.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery8() throws InterruptedException {
        log.info("testSequence8 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20], e2=Stream2[price>e1.price] or e3=Stream2[symbol=='IBM'] " +
                "select e1.price as price1, e2.price as price2, e3.price as price3 " +
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
                                Assert.assertArrayEquals(new Object[]{55.6f, null, 55.0f}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{55.0f, 57.6f, null}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream2.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.0f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery9() throws InterruptedException {
        log.info("testSequence9 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20], e2=Stream2[price>e1.price] or e3=Stream2[symbol=='IBM'] " +
                "select e1.price as price1, e2.price as price2, e3.price as price3 " +
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
                                Assert.assertArrayEquals(new Object[]{55.6f, 57.6f, null}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{57.6f, null, 55.7f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream2.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"IBM", 55.7f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery10() throws InterruptedException {
        log.info("testSequence10 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream2[price>20]+, e2=Stream1[price>e1[0].price] " +
                "select e1[0].price as price1, e1[1].price as price2, e2.price as price3 " +
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
                                Assert.assertArrayEquals(new Object[]{55.6f, null, 57.6f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 59.6f, 100});
        Thread.sleep(100);
        stream2.send(new Object[]{"WSO2", 55.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery11() throws InterruptedException {
        log.info("testSequence11 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], " +
                "   e2=Stream1[(e2[last].price is null and price>=e1.price) or ((not (e2[last].price is null)) and price>=e2[last].price)]+, " +
                "   e3=Stream1[price<e2[last].price] " +
                "select e1.price as price1, e2[0].price as price2, e2[1].price as price3, e3.price as price4 " +
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
                                Assert.assertArrayEquals(new Object[]{29.6f, 35.6f, 57.6f, 47.6f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 29.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 35.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 47.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery12() throws InterruptedException {
        log.info("testSequence12 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume int); " +
                "define stream TwitterStream (symbol string, count int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=StockStream[ price >= 50 and volume > 100 ], e2=TwitterStream[count > 10] " +
                "select e1.price as price, e1.symbol as symbol, e2.count as count " +
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
                                Assert.assertArrayEquals(new Object[]{76.6f, "IBM", 20}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler twitterStream = executionPlanRuntime.getInputHandler("TwitterStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"IBM", 75.6f, 105});
        stockStream.send(new Object[]{"GOOG", 51f, 101});
        stockStream.send(new Object[]{"IBM", 76.6f, 111});
        Thread.sleep(100);
        twitterStream.send(new Object[]{"IBM", 20});
        stockStream.send(new Object[]{"WSO2", 45.6f, 100});
        Thread.sleep(100);
        twitterStream.send(new Object[]{"GOOG", 20});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }


    @Test
    public void testQuery13() throws InterruptedException {
        log.info("testSequence13 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume int); " +
                "define stream TwitterStream (symbol string, count int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=StockStream[ price >= 50 and volume > 100 ], e2=StockStream[price <= 40]*, e3=StockStream[volume <= 70] " +
                "select e1.symbol as symbol1, e2[0].symbol as symbol2, e3.symbol as symbol3 " +
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
                                Assert.assertArrayEquals(new Object[]{"IBM", "GOOG", "WSO2"}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler twitterStream = executionPlanRuntime.getInputHandler("TwitterStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        stockStream.send(new Object[]{"GOOG", 21f, 81});
        stockStream.send(new Object[]{"WSO2", 176.6f, 65});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery14() throws InterruptedException {
        log.info("testSequence14 - OUT 3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream1 (symbol string, price float, volume int); " +
                "define stream StockStream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=StockStream1[ price >= 50 and volume > 100 ], e2=StockStream2[price <= 40]*, e3=StockStream2[volume <= 70] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as volume " +
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
                                Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{"WSO2", "DDD", 60}, event.getData());
                                break;
                            case 3:
                                Assert.assertArrayEquals(new Object[]{"DOX", null, 25}, event.getData());
                                break;
                            default:
                                Assert.assertSame(3, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stockStream1 = executionPlanRuntime.getInputHandler("StockStream1");
        InputHandler stockStream2 = executionPlanRuntime.getInputHandler("StockStream2");

        executionPlanRuntime.start();

        stockStream1.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        stockStream2.send(new Object[]{"GOOG", 21f, 81});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 65});
        stockStream1.send(new Object[]{"BIRT", 21f, 81});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165});
        stockStream2.send(new Object[]{"DDD", 23f, 181});
        stockStream2.send(new Object[]{"BIRT", 21f, 86});
        stockStream2.send(new Object[]{"BIRT", 21f, 82});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 60});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165});
        stockStream2.send(new Object[]{"DOX", 16.2f, 25});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 3, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery15() throws InterruptedException {
        log.info("testSequence15 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream1 (symbol string, price float, volume int); " +
                "define stream StockStream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=StockStream1[ price >= 50 and volume > 100 ], e2=StockStream2[e1.symbol != 'AMBA']*, e3=StockStream2[volume <= 70] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as volume " +
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
                                Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{"DOX", null, 25}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stockStream1 = executionPlanRuntime.getInputHandler("StockStream1");
        InputHandler stockStream2 = executionPlanRuntime.getInputHandler("StockStream2");

        executionPlanRuntime.start();

        stockStream1.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        stockStream2.send(new Object[]{"GOOG", 21f, 81});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 65});
        stockStream1.send(new Object[]{"BIRT", 21f, 81});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165});
        stockStream2.send(new Object[]{"DDD", 23f, 181});
        stockStream2.send(new Object[]{"BIRT", 21f, 86});
        stockStream2.send(new Object[]{"BIRT", 21f, 82});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 60});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165});
        stockStream2.send(new Object[]{"DOX", 16.2f, 25});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery16() throws InterruptedException {
        log.info("testSequence16 - OUT 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream1 (symbol string, price float, volume int); " +
                "define stream StockStream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=StockStream1, e2=StockStream2[e1.symbol != 'AMBA']*, e3=StockStream2[volume <= 70] " +
                "select e3.symbol as symbol1, e2[0].symbol as symbol2, e3.volume as volume " +
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
                                Assert.assertArrayEquals(new Object[]{"WSO2", "GOOG", 65}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{"DOX", null, 25}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler stockStream1 = executionPlanRuntime.getInputHandler("StockStream1");
        InputHandler stockStream2 = executionPlanRuntime.getInputHandler("StockStream2");

        executionPlanRuntime.start();

        stockStream1.send(new Object[]{"IBM", 75.6f, 105});
        Thread.sleep(100);
        stockStream2.send(new Object[]{"GOOG", 21f, 81});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 65});
        stockStream1.send(new Object[]{"BIRT", 21f, 81});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165});
        stockStream2.send(new Object[]{"DDD", 23f, 181});
        stockStream2.send(new Object[]{"BIRT", 21f, 86});
        stockStream2.send(new Object[]{"BIRT", 21f, 82});
        stockStream2.send(new Object[]{"WSO2", 176.6f, 60});
        stockStream1.send(new Object[]{"AMBA", 126.6f, 165});
        stockStream2.send(new Object[]{"DOX", 16.2f, 25});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery18() throws InterruptedException {
        log.info("testSequence18 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], " +
                "   e2=Stream1[((e2[last].price is null) and price>=e1.price) or ((not (e2[last].price is null)) and price>=e2[last].price)]+, " +
                "   e3=Stream1[price<e2[last].price] " +
                "select e1.price as price1, e2[0].price as price2, e2[1].price as price3, e3.price as price4 " +
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
                                Assert.assertArrayEquals(new Object[]{25.0f, 35.6f, 57.6f, 47.6f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 29.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 35.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 47.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery19() throws InterruptedException {
        log.info("testSequence19 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], " +
                "   e2=Stream1[((e2[last].price is null) and price>=e1.price) or ((not (e2[last].price is null)) and price>=e2[last].price)]+, " +
                "   e3=Stream1[price<e2[last].price] " +
                "select e1.price as price1, e2[0].price as price2, e2[1].price as price3, e3.price as price4 " +
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
                                Assert.assertArrayEquals(new Object[]{25.0f, 40.0f, null, 35.0f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 40.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 35.0f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery20() throws InterruptedException {
        log.info("testSequence20 - OUT 3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], " +
                "   e2=Stream1[((e2[last].price is null) and price>=e1.price) or ((not (e2[last].price is null)) and price>=e2[last].price)]+, " +
                "   e3=Stream1[price<e2[last].price] " +
                "select e1.price as price1, e2[0].price as price2, e2[1].price as price3, e3.price as price4 " +
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
                                Assert.assertArrayEquals(new Object[]{25.0f, 35.6f, null, 25.5f}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{25.5f, 57.6f, 58.6f, 47.6f}, event.getData());
                                break;
                            case 3:
                                Assert.assertArrayEquals(new Object[]{27.6f, 49.6f, null, 45.6f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(3, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 29.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 35.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 25.5f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 58.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 47.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 27.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 49.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 45.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 3, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery21() throws InterruptedException {
        log.info("testSequence21 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], " +
                "   e2=Stream1[((e2[last].price is null) and price>=e1.price) or ((not (e2[last].price is null)) and price>=e2[last].price)]+, " +
                "   e3=Stream1[price<e2[last].price] " +
                "select e1.price as price1, e2[0].price as price2, e2[last-2].price as price3, e2[last-1].price as price4, e2[last].price as price5, e3.price as price6, e2[last-20].price as price7 " +
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
                                Assert.assertArrayEquals(new Object[]{25.0f, 35.6f, 45.5f, 57.6f, 58.6f, 47.6f, null}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 29.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 35.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 45.5f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 58.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 47.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 45.6f, 100});
        Thread.sleep(10000);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery22() throws InterruptedException {
        log.info("testSequence22 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], " +
                "   e2=Stream1[((e2[last].price is null) and price>=e1.price) or ((not (e2[last].price is null)) and price>=e2[last].price)]+, " +
                "   e3=Stream1[price<e2[last].price and price>e2[last-1].price] " +
                "select e1.price as price1, e2[0].price as price2, e2[last-2].price as price3, e2[last-1].price as price4, e2[last].price as price5, e3.price as price6, e2[last-20].price as price7 " +
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
                                Assert.assertArrayEquals(new Object[]{25.0f, 35.6f, 45.5f, 57.6f, 58.6f, 57.7f, null}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 29.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 35.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 45.5f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 58.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 57.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 45.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 60.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 61.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 59.7f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery23() throws InterruptedException {
        log.info("testSequence23 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], " +
                "   e2=Stream1[price>=e2[last].price or price>=e1.price ]+, " +
                "   e3=Stream1[price<e2[last].price]" +
                "select e1.price as price1, e2[0].price as price2, e2[last-2].price as price3, e2[last-1].price as price4, e2[last].price as price5, e3.price as price6 " +
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
                                Assert.assertArrayEquals(new Object[]{25.0f, 35.6f, null, null, 35.6f, 29.5f}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{29.5f, 57.6f, null, 57.6f, 58.6f, 57.7f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 29.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 35.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 29.5f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 58.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 57.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 45.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }


    @Test
    public void testQuery24() throws InterruptedException {
        log.info("testSequence24 - OUT 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream Stream1 (symbol string, price float, volume int); " +
                "define stream Stream2 (symbol string, price float, volume int); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from every e1=Stream1[price>20], " +
                "   e2=Stream1[(price>=e2[last].price and (not (e2[last-1].price is null)) and price>=e2[last-1].price+5)  or ((e2[last-1].price is null) and price>=e1.price+5 )]+, " +
                "   e3=Stream1[price<e2[last].price]" +
                "select e1.price as price1, e2[0].price as price2, e2[last-2].price as price3, e2[last-1].price as price4, e2[last].price as price5, e3.price as price6 " +
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
                                Assert.assertArrayEquals(new Object[]{43.6f, 57.7f, null, 57.7f, 58.7f, 45.6f}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                    eventArrived = true;
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

        stream1.send(new Object[]{"WSO2", 29.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 25.0f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 35.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 41.5f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 42.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"WSO2", 43.6f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 57.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 58.7f, 100});
        Thread.sleep(100);
        stream1.send(new Object[]{"IBM", 45.6f, 100});
        Thread.sleep(100);

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testTimeBatchAndSequence() throws Exception {
        log.info("testTimeBatchAndSequence  OUT 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream received_reclamations (timestamp long, product_id string, defect_category string);";
        String query = "" +
                "@info(name = 'query1') " +
                "from received_reclamations#window.timeBatch(1 sec) " +
                "select product_id, defect_category, count(product_id) as num " +
                "group by product_id, defect_category " +
                "insert into reclamation_averages;" +
                "" +
                "@info(name = 'query2') " +
                "from a=reclamation_averages[num > 1], b=reclamation_averages[num > a.num and product_id == a.product_id and defect_category == a.defect_category] " +
                "select a.product_id, a.defect_category, a.num as oldNum, b.num as newNum " +
                "insert into increased_reclamations;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);


        executionPlanRuntime.addCallback("increased_reclamations", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                if (events != null) {
                    for (Event event : events) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{"abc", "123", 5l, 8l}, event.getData());
                                break;
                            default:
                                Assert.assertSame(1, inEventCount);
                        }
                    }
                }
                eventArrived = true;
            }
        });


        InputHandler i1 = executionPlanRuntime.getInputHandler("received_reclamations");

        executionPlanRuntime.start();


        for (int i = 0; i < 5; i++) {
            i1.send(new Object[]{System.currentTimeMillis(), "abc", "123"});
            Thread.sleep(100);
        }

        Thread.sleep(500);

        for (int i = 0; i < 8; i++) {
            i1.send(new Object[]{System.currentTimeMillis(), "abc", "123"});
            Thread.sleep(100);
        }
        Thread.sleep(1000);

        executionPlanRuntime.shutdown();

        Assert.assertEquals("Number of success events", 1, inEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }
}
