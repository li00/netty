package com.netty.netty.common;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.*;
import com.serotonin.modbus4j.sero.util.queue.ByteQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author Lizf
 * @date 2021/6/24
 */
@Slf4j
public class Modbus4jUtils {
    /**
     * 工厂
     */
    static ModbusFactory modbusFactory;

    /**
     * 建立链接
     */
    static ModbusMaster tcpMaster;
    static {
        if (modbusFactory == null) {
            modbusFactory = new ModbusFactory();
            tcpMaster = getMaster("192.168.0.103",5021);
        }
    }

    /**
     * 获取master
     *
     * @return
     * @throws ModbusInitException
     */
    public static ModbusMaster getMaster(String ip,Integer port){
        IpParameters params = new IpParameters();
        params.setHost(ip);
        params.setPort(port);
        //params.setHost("192.168.1.xxx");
        //params.setPort(502);
        //RTU 协议
        // modbusFactory.createRtuMaster(wapper);
        //UDP 协议
        // modbusFactory.createUdpMaster(params);
        //ASCII 协议
        // modbusFactory.createAsciiMaster(wrapper);
        // TCP 协议
        ModbusMaster  master = modbusFactory.createTcpMaster(params, true);
        try {
            master.setTimeout(5000);
            master.setRetries(3);
            master.init();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        }

        return master;
    }

    /**
     * 读取离散输入状态[02]
     * @param start  开始位
     * @param readLenth  位数
     * @return
     * @throws ModbusInitException
     */
    public static ByteQueue modbusTCP02(int slaveId,ModbusMaster tcpMaster,int start, int readLenth) throws ModbusInitException {
        //建立链接
        //ModbusMaster tcpMaster = getMaster(ip,port);
        //发送请求
        ModbusRequest modbusRequest=null;
        try {
            //功能码02
            modbusRequest = new ReadDiscreteInputsRequest(slaveId, start, readLenth);
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        //收到响应
        ReadResponse modbusResponse=null;
        try {
            modbusResponse = (ReadResponse) tcpMaster.send(modbusRequest);
            System.out.println(modbusResponse.getData());
            System.out.println(Arrays.toString(modbusResponse.getBooleanData()));
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        ByteQueue byteQueue= new ByteQueue(12);
        modbusResponse.write(byteQueue);
        System.out.println("功能码:"+modbusRequest.getFunctionCode());
        System.out.println("从站地址:"+modbusRequest.getSlaveId());
        System.out.println("开始地址:"+start);
        System.out.println("收到的响应信息大小:"+byteQueue.size());
        System.out.println("收到的响应信息值:"+byteQueue);
        return byteQueue;
    }

    /**
     * 读取离散输入状态[02]
     * @param start  开始位
     * @param readLength  位数
     * @return 返回boolean数组
     * @throws
     */
    public static boolean[] modbusTCP02Boolean(int slaveId, ModbusMaster tcpMaster, int start, int readLength) {
        ModbusRequest modbusRequest;
        ReadResponse modbusResponse;
        boolean [] ret = null;
        try {
            //功能码02
            modbusRequest = new ReadDiscreteInputsRequest(slaveId, start, readLength);
            modbusResponse = (ReadResponse) tcpMaster.send(modbusRequest);
            if(modbusResponse.isException()){
                log.error("读取异常 {}",modbusResponse.getExceptionMessage());
                return ret;
            }
            ret = modbusResponse.getBooleanData();
        } catch (ModbusTransportException e) {
            log.error("02功能码读取异常 ",e);
        }
        return ret;
    }


    /**
     * 读取保持寄存器[03]
     * @param start   开始地址
     * @param readLenth  读取数量
     * @return 返回short 数组
     * @throws
     */
    public static short[] modbusTCP03Short(int slaveId,ModbusMaster tcpMaster,int start, int readLenth) {
        ModbusRequest modbusRequest = null;
        ReadResponse modbusResponse = null;
        short [] ret = null;
        try {
            //功能码03
            modbusRequest = new ReadHoldingRegistersRequest(slaveId, start, readLenth);
            modbusResponse = (ReadResponse) tcpMaster.send(modbusRequest);
            ret = modbusResponse.getShortData();
        } catch (ModbusTransportException e) {
            log.error("03功能码读取异常 ",e);
        }
        return ret;
    }


    /**
     * 读取保持寄存器[03]
     * @param start   开始地址
     * @param readLenth  读取数量
     * @return
     * @throws ModbusInitException
     */
    public static ByteQueue modbusTCP03(int slaveId,ModbusMaster tcpMaster,int start, int readLenth) throws ModbusInitException {
        //建立链接
        //ModbusMaster tcpMaster = getMaster(ip,port);
        //发送请求
        ModbusRequest modbusRequest=null;
        try {
            modbusRequest = new ReadHoldingRegistersRequest(slaveId, start, readLenth);//功能码03
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        //收到响应
        ModbusResponse modbusResponse=null;
        try {
            modbusResponse = tcpMaster.send(modbusRequest);
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        ByteQueue byteQueue= new ByteQueue(12);
        modbusResponse.write(byteQueue);
        System.out.println("功能码:"+modbusRequest.getFunctionCode());
        System.out.println("从站地址:"+modbusRequest.getSlaveId());
        System.out.println("开始地址:"+start);
        System.out.println("收到的响应信息大小:"+byteQueue.size());
        System.out.println("收到的响应信息值:"+byteQueue);
        return byteQueue;
    }

    /**
     * 读取输入寄存器[04]
     * @param start   开始地址
     * @param readLenth  读取数量
     * @return
     * @throws ModbusInitException
     */
    public static ByteQueue modbusTCP04(int slaveId, ModbusMaster tcpMaster, int start, int readLenth) throws ModbusInitException {
        //建立链接
        // ModbusMaster tcpMaster = getMaster(ip,port);
        //发送请求
        ModbusRequest modbusRequest=null;
        try {
            modbusRequest = new ReadInputRegistersRequest(slaveId, start, readLenth);//功能码04
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        //收到响应
        ModbusResponse modbusResponse=null;
        try {
            modbusResponse = tcpMaster.send(modbusRequest);
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        ByteQueue byteQueue= new ByteQueue(12);
        modbusResponse.write(byteQueue);
        System.out.println("功能码:"+modbusRequest.getFunctionCode());
        System.out.println("从站地址:"+modbusRequest.getSlaveId());
        System.out.println("开始地址:"+start);
        System.out.println("收到的响应信息大小:"+byteQueue.size());
        System.out.println("收到的响应信息值:"+byteQueue);
        return byteQueue;
    }

    /**
     * 读取输入寄存器[04]
     * @param start   开始地址
     * @param readLenth  读取数量
     * @return
     * @throws ModbusInitException
     */
    public static short[] modbusTCP04Short(int slaveId, ModbusMaster tcpMaster, int start, int readLenth){
        ModbusRequest modbusRequest = null;
        ReadResponse modbusResponse = null;
        short [] ret = null;
        try {
            //功能码04
            modbusRequest = new ReadInputRegistersRequest(slaveId, start, readLenth);
            modbusResponse = (ReadResponse) tcpMaster.send(modbusRequest);
            ret = modbusResponse.getShortData();
            System.out.println("功能码:"+modbusRequest.getFunctionCode());
            System.out.println("从站地址:"+modbusRequest.getSlaveId());
            System.out.println("开始地址:"+start);
            System.out.println("收到的响应信息值:"+Arrays.toString(ret));
        } catch (ModbusTransportException e) {
            log.error("04功能码读取异常 ",e);
        }
        return ret;
    }


    /**
     * 写单个线圈[05]
     * @param writeOffset  开始位
     * @param writeValue  true false
     * @return
     * @throws ModbusInitException
     */
    public static ByteQueue modbusTCP05(int slaveId,ModbusMaster tcpMaster,int writeOffset, boolean writeValue) throws ModbusInitException {
        //建立链接
        //ModbusMaster tcpMaster = getMaster(ip,port);
        //发送请求
        ModbusRequest modbusRequest=null;
        try {
            //功能码05
            modbusRequest = new WriteCoilRequest(slaveId, writeOffset, writeValue);
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        //收到响应
        ModbusResponse modbusResponse=null;
        try {
            modbusResponse = tcpMaster.send(modbusRequest);
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        ByteQueue byteQueue= new ByteQueue(12);
        modbusResponse.write(byteQueue);
        System.out.println("功能码:"+modbusRequest.getFunctionCode());
        System.out.println("从站地址:"+modbusRequest.getSlaveId());
        System.out.println("收到的响应信息大小:"+byteQueue.size());
        System.out.println("收到的响应信息值:"+byteQueue);
        return byteQueue;
    }

}
