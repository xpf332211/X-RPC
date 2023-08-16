package com.meiya.utils;

import com.meiya.utils.print.Out;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.LongAdder;

/**
 * 分布式id生成器
 * 雪花算法
 *
 * @author xiaopf
 */
@Slf4j
public class IdGenerator {
    /**
     * 起始时间戳
     */
    public static final long START_TIMESTAMP = DateUtils.getDate("2002-07-30").getTime();

    /**
     * 机房号(数据中心)占用比特位 最多2^5个机房
     */
    public static final int DATE_CENTER_BIT = 5;
    /**
     * 机器号占用比特位 每个机房最多2^5台机器
     */
    public static final int MACHINE_BIT = 5;
    /**
     * 时间戳占用比特位 每台机器最多生成2^42个毫秒值 可表示约139年
     */
    public static final int TIMESTAMP_BIT = 42;
    /**
     * 递增序列号占用比特位 每个毫秒值中最多产生2^12个序列号
     */
    public static final int SEQUENCE_BIT = 12;

    /**
     * 机房号的最大值 2^5 - 1
     */
    public static final long DATE_CENTER_MAX = (~(-1L << DATE_CENTER_BIT));
    /**
     * 机器号的最大值 2^5 - 1
     */
    public static final long MACHINE_MAX = (~(-1L << MACHINE_BIT));
    /**
     * 时间戳的最大值 2^42 - 1
     */
    public static final long TIMESTAMP_MAX = (~(-1L << TIMESTAMP_BIT));
    /**
     * 序列号的最大值 2^12 - 1
     */
    public static final long SEQUENCE_MAX = (~(-1L << SEQUENCE_BIT));

    //000001001000000100100000010010000001001001   00011     10110     0000010010000001001001
    //               42位时间戳                    5位机房号   5位机器号         12位序列号
    //                            总共64位 8个字节 long类型的分布式id

    /**
     * 时间戳需要左移5+5+12位
     */
    public static final int TIMESTAMP_LEFT_MOVE = DATE_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    /**
     * 机房号需要左移5+12位
     */
    public static final int DATE_CENTER_LEFT_MOVE = MACHINE_BIT + SEQUENCE_BIT;
    /**
     * 机器号需要左移12位
     */
    public static final int MACHINE_LEFT_MOVE = SEQUENCE_BIT;

    private final long dataCenterId;
    private final long machineId;
    private final LongAdder timestampAdderId = new LongAdder();
    private final LongAdder sequenceAdderId = new LongAdder();
    private final LongAdder lastTimestampAdderId = new LongAdder();

    public IdGenerator(long dataCenterId, long machineId) {
        //检查参数是否合法
        if (dataCenterId > DATE_CENTER_MAX || dataCenterId < 0) {
            throw new IllegalArgumentException("机房号不合法");
        }
        if (machineId > MACHINE_MAX || machineId < 0) {
            throw new IllegalArgumentException("机器号超过最大值！");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {
        //获取当前时间戳
        this.timestampAdderId.reset();
        this.timestampAdderId.add(System.currentTimeMillis() - START_TIMESTAMP);

        //当前时间戳小于上一次的时间戳 进行了时钟回拨
        if (timestampAdderId.sum() < lastTimestampAdderId.sum()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.error("时钟回拨延迟等待时发生了异常");
            }
            //一次重试 再次获取当前时间戳
            this.timestampAdderId.reset();
            this.timestampAdderId.add(System.currentTimeMillis() - START_TIMESTAMP);
            //时间恢复 再次生成id 否则抛出异常
            if (timestampAdderId.sum() > lastTimestampAdderId.sum()){
                return getId();
            }else {
                throw new RuntimeException("系统进行了时钟回拨！");
            }
            //同一时间戳 需要生成不同的序列号
        } else if (timestampAdderId.sum() == lastTimestampAdderId.sum()) {
            sequenceAdderId.increment();
            //序列号超过最大值 将时间戳设置为下一个时间戳 序列号恢复0L （不能直接时间戳+1）
            if (sequenceAdderId.sum() > SEQUENCE_MAX) {
                long nextTimestamp = getNextTimestamp();
                timestampAdderId.reset();
                timestampAdderId.add(nextTimestamp);
                sequenceAdderId.reset();
            }
            //时间戳大于上一次时间戳 序列号恢复0L
        } else {
            sequenceAdderId.reset();
        }
        //记录上一个时间戳
        lastTimestampAdderId.reset();
        lastTimestampAdderId.add(timestampAdderId.sum());

        long sequenceId = sequenceAdderId.sum();
        long timestampId = timestampAdderId.sum();
        return (timestampId << TIMESTAMP_LEFT_MOVE) | (dataCenterId << DATE_CENTER_LEFT_MOVE)
                | (machineId << MACHINE_LEFT_MOVE) | (sequenceId);

    }

    private long getNextTimestamp() {
        //循环获取时间戳 直到进入下一个时间戳
        while (timestampAdderId.sum() == lastTimestampAdderId.sum()) {
            timestampAdderId.reset();
            timestampAdderId.add(System.currentTimeMillis() - START_TIMESTAMP);
        }
        return timestampAdderId.sum();
    }


}
