#include <SoftwareSerial.h>

SoftwareSerial btSerial(9, 10); // TX, RX

// Global
String BUFFER = "";
char DELIMETER = ':';
int LED = 13;

// Defaults
String TYPE = "blink";
int TIME = 100;  // 500, 250, 100, 50 (milliseconds)
int LOOP = 3;    // 3, 2, 1
int REPT = 15000;   // 30000, 15000, 5000, 3000 (milliseconds)
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
    }
    
    Serial.println("BUFFER: "+BUFFER);
    String buff = "";
    int parsed = 0;
    for(int i=0; i<BUFFER.length(); i++) {
      // copy buffer
      buff += BUFFER[i];
      
      // clear buffer
      if(BUFFER[i] == ':') {
        buff = "";
      }
      
      // get a value
      if(BUFFER[i+1] == ':') {
        if(parsed == 0) {
          type = buff;
          parsed++;
        }
        else if(parsed == 1) {
          looper = buff.toInt();
          parsed++;
        }
        else if(parsed == 2) {
          time = buff.toInt();
          parsed++;
        }
        buff = "";
      }
      
      // get last value
      if(i == (BUFFER.length()-1)) {
        repeat = buff.toInt();
      }
    }
    BUFFER = "";
    
    // type+":"+loop+":"+time+":"+repeat
    Serial.println(type+":"+looper+":"+time+":"+repeat);
    btSerial.write(time);
    
    // validation
    // type["blink", "pulse"]
    // time[500, 250, 100, 50]
    // loop[1, 2, 3]
    // rept[30, 15, 5, 3]
    if(type == "blink") {
      blink(time, looper);
    }
    else if(type == "pulse") {
      blink(time, looper);
    }
    else {
      // error in data, perform default
      blink(time, looper);
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
//   time: int - time for the delay between on/off
//   looper: int - amount of times blink the LED
void blink(int time, int looper) {
  for(int i=0; i<looper; i++) {
    digitalWrite(LED, HIGH);
    delay(time);
    digitalWrite(LED, LOW);
    delay(time);
  }
}

// pulse - blinks an LED
// params:
//   time: int - time for the delay between on/off
//   looper: int - amount of times pulse the LED
void pulse(int time, int looper) {
  // same as above, but with pwm
}

// reapeat - repeats a function
// params:
//   rept: int - milliseconds to call function
void repeat(int rept) {
  int time = millis();
  
  while(REPEATING == true) {
    int current = millis();
    
    if(current - time > rept) {
      Serial.println("repeating");
    }
  }
}
