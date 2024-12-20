package com.game.pusher;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Pusher {
	private int version = 1;
	private int appId = 1;
	private int timeout;
	
	private String host;
	private int port ;
	private Socket socket;
	private InputStream in ;
	private OutputStream out ;
	
	public Pusher(String host, int port, int timeoutMillis, int version, int appId) throws Exception{
		this.setVersion(version);
		this.setAppId(appId);
		this.host = host;
		this.port = port;
		this.timeout = timeoutMillis;
		initSocket();
	}
	
	public Pusher(String host, int port, int timeoutMillis) throws Exception{
		this(host,port,timeoutMillis,1,1);
	}
	
	public Pusher(Socket socket)throws Exception{
		this.socket = socket;
		in = socket.getInputStream();
		out = socket.getOutputStream();
	}
	
	private void initSocket()throws Exception{
		socket = new Socket(this.host, this.port);
		socket.setSoTimeout(timeout);
		in = socket.getInputStream();
		out = socket.getOutputStream();
	}
	
	public void close() throws Exception{
		if(socket == null){
			return;
		}
		socket.close();
	}
	
	public void setVersion(int version) throws java.lang.IllegalArgumentException{
		if(version < 1 || version > 255){
			throw new java.lang.IllegalArgumentException("version must be 1 to 255");
		}
		this.version = version;
	}
	
	public int getVersion(){
		return this.version;
	}
	
	public void setAppId(int appId) throws IllegalArgumentException{
		if(appId < 1 || appId > 255){
			throw new java.lang.IllegalArgumentException("appId must be 1 to 255");
		}
		this.appId = appId;
	}
	
	public int getAppId(){
		return this.appId;
	}
	
	private boolean checkUuidArray(byte[] uuid) throws IllegalArgumentException{
		if(uuid == null || uuid.length != 16){
			throw new IllegalArgumentException("uuid byte array must be not null and length of 16");
		}
		return true;
	}
	
	private boolean checkLongArray(byte[] longArray) throws IllegalArgumentException{
		if(longArray == null || longArray.length != 8){
			throw new IllegalArgumentException("array must be not null and length of 8");
		}
		return true;
	}
	
	public boolean push0x10Message(byte[] uuid) throws Exception{
		checkUuidArray(uuid);
		out.write(version);
		out.write(appId);
		out.write(16);
		out.write(uuid);
		out.write(0);
		out.write(0);
		out.flush();
		
		byte[] b = new byte[1];
		in.read(b);
		if((int)b[0] == 0){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean push0x11Message(byte[] uuid, long data) throws Exception{
		byte[] tmp = new byte[8];
		ByteBuffer.wrap(tmp).putLong(data);
		return this.push0x11Message(uuid, tmp);
	}
	
	public boolean push0x11Message(byte[] uuid, byte[] data) throws Exception{
		this.checkLongArray(data);
		out.write(version);
		out.write(appId);
		out.write(17);
		out.write(uuid);
		out.write(0);
		out.write(8);
		out.write(data);
		out.flush();
		
		byte[] b = new byte[1];
		in.read(b);
		if((int)b[0] == 0){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean push0x20Message(byte[] uuid, byte[] data) throws Exception{
		this.checkUuidArray(uuid);
		if(data == null){
			throw new NullPointerException("data array is null");
		}
		if(data.length == 0 || data.length > 500){
			throw new IllegalArgumentException("data array length illegal, min 1, max 500");
		}
		byte[] dataLen = new byte[2];
		ByteBuffer.wrap(dataLen).putChar((char)data.length);
		out.write(version);
		out.write(appId);
		out.write(32);
		out.write(uuid);
		out.write(dataLen);
		
		out.write(data);
		out.flush();
		
		byte[] b = new byte[1];
		in.read(b);
		if((int)b[0] == 0){
			return true;
		}else{
			return false;
		}
		
	}
	
	public static void main(String[] args){
		Pusher pusher = null;
		try{
			pusher = new Pusher("192.168.0.199",9999, 1000);
//            pusher.push0x20Message(StringUtil.hexStringToByteArray("41299de22c46b9f179051414a408afc0"),"李言则,你的国家被打了!".getBytes("UTF-8"));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(pusher != null){
				try{
				    pusher.close();
				}catch(Exception e) {

                }
			}
		}
	}

}
