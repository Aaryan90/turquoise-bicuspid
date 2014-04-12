#include <SoftwareSerial.h>

SoftwareSerial btSerial(9, 10); // TX, RX

// Global
String BUFFER = "";
char DELIMETER = ':';
int LED = 13;
int TIME_START = 0;

// Defaults
String TYPE = "blink";   // blink, pulse
int LOOP = 3;            // 3, 2, 1
int TIME = 100;          // 500, 250, 100, 50 (milliseconds)
int REPT = 15000;        // 30000, 15000, 5000, 3000, -1 (milliseconds)
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
  // read from bluetooth
  if(btSerial.available()) {
    while(btSerial.available()) {
      BUFFER += (char)btSerial.read();
      delay(1);
    }
    
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
          TYPE = buff;
          index++;
        }
        else if(index == 1) {
          LOOP = buff.toInt();
          index++;
        }
        else if(index == 2) {
          TIME = buff.toInt();
          index++;
        }
        buff = "";
      }
      
      // get last value
      if(i == (BUFFER.length()-1)) {
        REPT = buff.toInt();
        if(REPT > 0)
        {
          REPEATING = true;
        }
        else if(REPT == -1) {
          REPEATING = false;
        }
      }
    }
    BUFFER = "";
    
    Serial.println(TYPE+":"+LOOP+":"+TIME+":"+REPT);
    //btSerial.write(TIME);
    
    // validation
    // TYPE["blink", "pulse"]
    // TIME[500, 250, 100, 50]
    // LOOP[1, 2, 3]
    // REPT[30, 15, 5, 3, -1]
    if(TYPE == "blink") {
      blink(LOOP, TIME);
    }
    else if(TYPE == "pulse") {
      blink(LOOP, TIME);
    }
    else if(TYPE == "clear") {
      REPEATING = false;
    }
    else {
      blink(LOOP, TIME);
    }
  }
  
  int time_end = millis();
  int time_diff = time_end - TIME_START;
  if(time_diff > REPT) {
    TIME_START = millis();
    if(REPEATING) {
      Serial.println("repeat");
      blink(LOOP, TIME);
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
