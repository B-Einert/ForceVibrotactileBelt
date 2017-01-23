//Constants
#define RAZOR Serial2
#define PIN_STATUS 13               //status led     
#define fsr1 A0                     //fsr

int LED_status = HIGH;
int pressure;                       //pressure unit
int ffsr1 = 0;
String s1="01";
String n="\n";
char msg;

void setup() {
  Serial.begin(57600);               //serial port initialization
  Serial.println("Bluetooth Serial initialized");
  pinMode(PIN_STATUS, OUTPUT);
  RAZOR.begin(57600);
}

void loop() {
  if (RAZOR.available()){
    while(RAZOR.available()>0){
      msg=RAZOR.read();
      if(msg=='#'){
        Serial.print(msg);
        if (RAZOR.available()){
          while(RAZOR.available()>0){
            msg=RAZOR.read();
            if(msg=='#'){
              if (RAZOR.available()){
                while(RAZOR.available()>0){
                  RAZOR.read();
                }
              }
            }
            else{
              Serial.print(msg);
            }
          }
        }
      }
    }
  }
  
  digitalWrite(PIN_STATUS,LED_status);
  delay(200);
}

void writeString(String stringData) { // Used to serially push out a String with Serial.write()

  for (int i = 0; i < stringData.length(); i++)
  {
    RAZOR.write(stringData[i]);   // Push each char 1 by 1 on each loop pass
  }
}

