package movecursor;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class MoveCursor {
    static double[] screen;
    public static int[] keys;
    
    Robot robot = new Robot();
    
    public MoveCursor() throws AWTException{
        robot.setAutoDelay(40);
        robot.setAutoWaitForIdle(true);
        int[] key = new int[26];
        key[0]=KeyEvent.VK_A;
        key[1]=KeyEvent.VK_B;
        key[2]=KeyEvent.VK_C;
        key[3]=KeyEvent.VK_D;
        key[4]=KeyEvent.VK_E;
        key[5]=KeyEvent.VK_F;
        key[6]=KeyEvent.VK_G;
        key[7]=KeyEvent.VK_H;
        key[8]=KeyEvent.VK_I;
        key[9]=KeyEvent.VK_J;
        key[10]=KeyEvent.VK_K;
        key[11]=KeyEvent.VK_L;
        key[12]=KeyEvent.VK_M;
        key[13]=KeyEvent.VK_N;
        key[14]=KeyEvent.VK_O;
        key[15]=KeyEvent.VK_P;
        key[16]=KeyEvent.VK_Q;
        key[17]=KeyEvent.VK_R;
        key[18]=KeyEvent.VK_S;
        key[19]=KeyEvent.VK_T;
        key[20]=KeyEvent.VK_U;
        key[21]=KeyEvent.VK_V;
        key[22]=KeyEvent.VK_W;
        key[23]=KeyEvent.VK_X;
        key[24]=KeyEvent.VK_Y;
        key[25]=KeyEvent.VK_Z;
        
        keys=key;
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        
        double[] result = {width,height};
        screen = result;
    }

    public static void print(String s){
        System.out.println(s);
    }
    
    public static double screenX(){
        return MoveCursor.screen[0];
    }
    
    public static double screenY(){
        return MoveCursor.screen[1];
    }
    
    public static double getMouseX(){
        return MouseInfo.getPointerInfo().getLocation().x;
    }
    
    public static double getMouseY(){
        return MouseInfo.getPointerInfo().getLocation().y;
    }
    
    //move from current
    public void moveMouse(double x,double y){
        try{
            robot.mouseMove((int)(getMouseX()+x),(int)(getMouseY()+y));
        }catch(Exception e){
            
        }
    }
    
    public void moveTo(double x,double y){
        try{
            x = Math.max(x, 0);
            y = Math.max(y, 0);
            robot.mouseMove((int)(x),(int)(y));
        }catch(Exception e){
        }
    }
    
    public void moveSmooth(int toX,int toY) {
        
        Thread t1 = new Thread(() -> {
            double fromX = getMouseX();
            double fromY = getMouseY();
            
            int smooth = 100;
            assert(smooth>2);
            
            double dx = (toX-fromX)/smooth;
            double dy = (toY-fromY)/smooth;
            
            
            for(int i = 1;i<=smooth;++i){
                this.moveMouse(dx,dy);
                try{
                    Thread.sleep(1);
                }catch(InterruptedException e){
                }
                
            }
        });
        t1.start();
        
        
    }
    
    public void click(){
        try{
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(200);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            robot.delay(200);
            
        }catch(Exception e){
        }
    }
    
    public void recordMouse() {
        Thread t1 = new Thread(new Runnable() {
            public void run(){
                try{
                    while(true){
                        print(Double.toString(getMouseX()));
                        print(Double.toString(getMouseY()));
                        Thread.sleep(500);
                    }
                }catch(InterruptedException e){ 
                }
            }
        });
        t1.start();
    }  
    
    public static int getKey(char c){
        int ch=(int)c;
        ch=ch-97;
        return keys[ch];
    }
    
    public void pressKey(int key) {
        try{
            robot.delay(40);
            robot.keyPress(key);
            robot.keyRelease(key);
        }catch(Exception e){
        }
        
    }
    
    public void shiftDown(){
        robot.keyPress(KeyEvent.VK_SHIFT);
    }
    
    public void shiftUp(){
        robot.keyRelease(KeyEvent.VK_SHIFT);
    }
    
    public void writeCapitalLetter(char c){
        assert((c>=65 && c<=97) || (c>=97 && c<=122));
        
        if(c>=97 && c<=122){
            c-=32;
        }
        
        int key = getKey((char)(c+32));
        shiftDown();
        pressKey(key);
        shiftUp();
        
    }
    
    public void writeSmallLetter(char c){
        assert((c>=65 && c<=97) || (c>=97 && c<=122));
        
        if(c>=65 && c<=97){
            c+=32;
        }
        
        int key = getKey((char)(c));
        pressKey(key);
        
    }
    
    public static boolean isSmallLetter(char c){
        return (c>=97 && c<=122);
    }
    
    public static boolean isCapitalLetter(char c){
        return (c>=65 && c<=97);
    }
    
    public void writeText(String text){
        for(int i=0;i<text.length();++i){
            
            char c = text.charAt(i);
            
            if(isSmallLetter(c)){
                this.writeSmallLetter(c);
            }else{
                if(isCapitalLetter(c)){
                    this.writeCapitalLetter(c);
                }else{
                    if(c==32){
                        this.pressKey(KeyEvent.VK_SPACE);
                    }
                }
            }
            
        }
    }
    
    public static void wait(double c){
        try{
            Thread.sleep((int)(c*1000));
        }catch(InterruptedException e){
        }
    }
    
    public static void searchYoutube(String url) {
        try {
            String result="https://www.youtube.com/results?search_query=";
            url = url.replace(" ", "+");
            
            Desktop desktop = Desktop.getDesktop();
            if(desktop == null){
                System.err.println("No desktop found");
                return;
            }
            
            Desktop.getDesktop().browse(new URL(result+url).toURI());
        } catch (URISyntaxException | IOException e) {
        }
    }
    
    public static void searchGoogle(String url) {
        try {
            String result="https://www.google.com/search?q=";
            url = url.replace(" ", "%20");
            
            Desktop desktop = Desktop.getDesktop();
            if(desktop == null){
                System.err.println("No desktop found");
                return;
            }
            
            Desktop.getDesktop().browse(new URL(result+url).toURI());
        } catch (URISyntaxException | IOException e) {
        }
    }
    
    public void backspace(){
        pressKey(KeyEvent.VK_BACK_SPACE);
    }
    
    public void scrollUp(){
        robot.mouseWheel(3);
    }
    
    public void scrollDown(){
        robot.mouseWheel(-3);
    }
    
    public void enter(){
        pressKey(KeyEvent.VK_ENTER);
    }
    
    public static void searchLink(String urlString) {
        try {
            Desktop desktop = Desktop.getDesktop();
            if(desktop == null){
                System.err.println("No desktop found");
                return;
            }
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws InterruptedException, AWTException {
        //*** for testing, first declare new MoveCursor object ***//
    }
}