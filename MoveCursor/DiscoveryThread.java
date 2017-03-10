package movecursor;

import com.sun.istack.internal.logging.Logger;
import java.awt.AWTException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;


public class DiscoveryThread implements Runnable{
    
    public static DiscoveryThread getInstance(){
        return DiscoveryThreadHolder.INSTANCE;
    }
    
    private static class DiscoveryThreadHolder{
        private static final DiscoveryThread INSTANCE = new DiscoveryThread();
    }
    
    
    //*** client screen size ***//
    int externalDeviceX;
    int externalDeviceY;
    
    //*** when finger lifted, save position on server (this device) ***//
    int stopX;
    int stopY;
    DatagramSocket socket;
    
    @Override
    public void run(){
        
        try{
            MoveCursor move = new MoveCursor();
            
            //*** listen on all local IP's with 0.0.0.0 *///
            socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
            //*** necessary for IPv4 before being able to broadcast, it sets a flag to packets ***//
            socket.setBroadcast(true);
            
            
            while(true){
                System.out.println(getClass().getName() + "Ready to receive broadcast");
                
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf,recvBuf.length);
                
                //*** receive blocks current Thread => dont use in UI Thrread on android ***//
                socket.receive(packet);
                
                
                //*** System.out.println(getClass().getName() + "data: " + new String(packet.getData()).trim()); ***//
                
                String message = new String(packet.getData()).trim();
                
                if(message.equals("DISCOVER_SERVER")){
                    byte[] sendData = "RESPONSE".getBytes();
                    
                    //*** send response ***//
                    DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,packet.getAddress(),packet.getPort());
                    socket.send(sendPacket);
                    
                }
                
                if(message.substring(0, 4).equals("LINK")){
                    String link = message.substring(4,message.length());
                    MoveCursor.searchLink(link);
                }
                
                
                String[] positions = message.split("/");
                
                if(positions.length==2){
                    move.moveTo(stopX+Integer.parseInt(positions[0]), stopY+Integer.parseInt(positions[1]));
                }
                
                if(positions.length==3){
                    if(positions[0].equals("DIMENSIONS")){
                        externalDeviceX = Integer.parseInt(positions[1]);
                        externalDeviceY = Integer.parseInt(positions[2]);
                    }
                       
                    
                    if(positions[0].equals("STOP")){
                        stopX = (int)MoveCursor.getMouseX();
                        stopY = (int)MoveCursor.getMouseY();
                        
                    }
                    
                            
                    if(positions[0].equals("CLICK")){
                        move.click();
                    }
                    
                    if(positions[0].equals("TEXT")){
                        move.writeText(positions[1]);
                        
                    }
                    
                    if(positions[0].equals("YOUTUBE")){
                        MoveCursor.searchYoutube(positions[1]);
                    }
                    
                    if(positions[0].equals("GOOGLE")){
                        MoveCursor.searchGoogle(positions[1]);
                    }
                    
                    if(positions[0].equals("UP")){
                        move.scrollUp();
                    }
                    
                    if(positions[0].equals("DOWN")){
                        move.scrollDown();
                    }
                    
                    if(positions[0].equals("ENTER")){
                        move.enter();
                    }
                    
                    if(positions[0].equals("BACKSPACE")){
                        move.backspace();
                    }
                    
                    if(positions[0].equals("HALT")){
                        break;
                    }
                }
                
            }
            socket.close();
            
        }catch(IOException ex){
            Logger.getLogger(DiscoveryThread.class).log(Level.SEVERE,null,ex);
        } catch (AWTException ex) {
            java.util.logging.Logger.getLogger(DiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch(NumberFormatException e){
        }finally{
        }
    }
    
    public static void main(String[] args){
        Thread server = new Thread(DiscoveryThread.getInstance());
        server.start();
    }
}
