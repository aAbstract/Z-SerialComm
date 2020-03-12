import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.lang.Thread;
import java.util.Scanner;

public class main {

  public static void log(String data) {
    System.out.println(data);
  }

  public static interface serial_read_event_handler {
    public void execute(String data);
  }

  public static class read_process extends Thread {
    InputStream in;
    main.serial_read_event_handler h;

    public read_process(InputStream _in, main.serial_read_event_handler handler){
      this.in = _in;
      this.h = handler;
      this.start();
    }

    public void run() {
      while (true) {
        try {
          if (this.in.available() > 0) {
            Integer d = new Integer(this.in.read());
            String data = d.toString();
            this.h.execute(data);
          }
        } catch (IOException e) {
          main.log(e.getMessage());
        }
      }
    }
  }

  public static class serial_com {

    SerialPort sp = null;

    serial_com(String port, int rate, int pin1, int pin2) {
      main.log("Initilizing Serial Port Communcation...");
      this.sp = SerialPort.getCommPort(port);
      sp.setComPortParameters(rate, 8, pin1, pin2);
      //sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
      //sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
      if (sp.openPort()) {
        main.log("Serial Port " + port + " Connected");
        main.log("Starting Async Based System...");
        new main.read_process(this.sp.getInputStream(), new serial_read_event_handler() {
          @Override
          public void execute(String in_data) {
            main.log(in_data);
          }
        });
        main.log("Async System Started");
      } else {
        main.log("Serial Port " + port + " Connection Refuesed");
      }
    }

    public void write_data(byte data) {
      try {
        this.sp.getOutputStream().write(data);
        this.sp.getOutputStream().flush();
        main.log("[SERIAL-PORT]: Data -> " + ((int)data));
      } catch (IOException e) {
        main.log("[ERROR]: " + e.getMessage());
      }
    }

    protected void finalize() {
      if (this.sp.closePort()) {
        main.log("[SERIAL-PORT]: Serial Communcation Closed");
      } else {
        main.log("[ERROR]: Serial Communcation NOT Closed");
      }
    }
  }

  public static void main(String[] args) {
    try {
      main.serial_com s = new main.serial_com("/dev/ttyACM0", 9600, 1, 0);
      Scanner stdin = new Scanner(System.in);
      while (true) {
        String ans = stdin.nextLine();
        s.write_data(new Integer(ans).byteValue());
      }
    } catch (Exception e) {
      main.log(e.getMessage());
    }
  }
}
