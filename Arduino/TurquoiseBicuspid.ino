#include <SoftwareSerial.h>

SoftwareSerial btSerial(9, 10); // TX, RX

// Global
String BUFFER = "";
String BUFFSTORE = "";
char DELIMETER = ':';
int LED = 13;
int TIME_START = 0;

// Defaults
String TYPE = "blink";   // blink, pulse
int TIME = 100;          // 500, 250, 100, 50 (milliseconds)
int LOOP = 3;            // 3, 2, 1
int REPT = 15000;        // 30000, 15000, 5000, 3000 (milliseconds)
boolean REPEATING = false;

void setup()
{
   pinMode(LED, OUTPUT); 
  
   Serial.begin(9600);
   Serial.println("Type AT commands!");

   // JY-MCU v1.08 defaults to 9600.
   btSerial.begin(9600);
   btSerial.write("AT+BAUD4");
   btSerial.write("AT+NAMETurquoiseBicuspid");
   btSerial.write("AT+PIN1234");
   
   TIME_START = millis();
}

void loop()
{
  String type = "";
  int time = 0;
  int looper = 0;
  int repeat = 0;
  // read from bluetooth
  if(btSerial.available()) {
    while(btSerial.available()) {
      BUFFER += (char)btSerial.read();
      delay(1);
    }
    BUFFSTORE = BUFFER;
    
    Serial.println("BUFFER: "+BUFFER);
    String buff = "";
    int index = 0;
    for(int i=0; i<BUFFER.length(); i++) {
      // copy buffer
      buff += BUFFER[i];
      
      // clear buffer
      if(BUFFER[i] == ':') {
        buff = "";
      }
      
      // get a value
      if(BUFFER[i+1] == ':') {
        if(index == 0) {
          type = buff;
          index++;
        }
        else if(index == 1) {
          looper = buff.toInt();
          index++;
        }
        else if(index == 2) {
          time = buff.toInt();
          index++;
        }
        buff = "";
      }
      
      // get last value
      if(i == (BUFFER.length()-1)) {
        repeat = buff.toInt();
        REPEATING = true;
      }
    }
    BUFFER = "";
    
    Serial.println(type+":"+looper+":"+time+":"+repeat);
    btSerial.write(time);
    
    // validation
    // type["blink", "pulse"]
    // time[500, 250, 100, 50]
    // loop[1, 2, 3]
    // rept[30, 15, 5, 3]
    if(type == "blink") {
      blink(looper, time);
    }
    else if(type == "pulse") {
      blink(looper, time);
    }
    else if(type == "clear") {
      REPEATING = false;
    }
    else {
      blink(looper, time);
    }
  }
  
  int time_end = millis();
  int time_diff = time_end - TIME_START;
  if(time_diff > 5000) {
    TIME_START = millis();
    if(REPEATING) {
      Serial.println("repeat");
      blink(looper, time);
    }
  }
  
  // AT commands
  if(Serial.available()){
    delay(10);
    btSerial.write(Serial.read());
  }
}

// blink - blinks an LED
// params:
//   looper: int - amount of times blink the LED
//   time: int - time for the delay between on/off
void blink(int looper, int time) {
  for(int i=0; i<looper; i++) {
    digitalWrite(LED, HIGH);
    delay(time);
    digitalWrite(LED, LOW);
    delay(time);
  }
}

// pulse - blinks an LED
// params:
//   looper: int - amount of times pulse the LED
//   time: int - time for the delay between on/off
void pulse(int looper, int time) {
  // same as above, but with pwm
}
