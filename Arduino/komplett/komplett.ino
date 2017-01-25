//Constants
#define BLUE Serial3
#define RAZOR Serial2
#define PIN_STATUS 13   //status led   
  
//fsr initialization (analog pins start at 54)
int fsr[8] = {54, 55, 56, 59, 61, 62, 64, 65};
bool fsrzero[8] ={true, true, true, true, true, true, true, true};
String tag[8] ={"01", "02", "03", "04", "05", "06", "07", "08"};

int LED_status = HIGH;
int pressure;
int currentfsr;

String n="#";
String comma=",";
String msg="";
String singleMsg;
char imumsg;

void setup() {
  Serial.begin(9600);               //serial port initialization
  Serial.println("Bluetooth Serial initialized");
  pinMode(PIN_STATUS, OUTPUT);
  
  BLUE.begin(9600);
  RAZOR.begin(57600);
}

void loop() {
  //fsr read
  for(int i=0; i<8; i++){
    currentfsr=fsr[i];
    pressure = analogRead(currentfsr);
    if(pressure>20){
      singleMsg= tag[i]+pressure+comma+millis()+n;
      msg+=singleMsg;
      fsrzero[i]=false;
    }
    else{
      if(fsrzero[i]==false){
        singleMsg= tag[i]+"0"+comma+millis()+n;
        msg+=singleMsg;
        fsrzero[i]=true;
      }
    }
  }
  writeString(msg);
  Serial.print(msg);
  msg="";
  delay(200);

  //imu read
  if (RAZOR.available()){
    while(RAZOR.available()>0){
      imumsg=RAZOR.read();
      if(imumsg=='#'){
        if (RAZOR.available()){
          while(RAZOR.available()>0){
            imumsg=RAZOR.read();
            if(imumsg=='#'){
              BLUE.write(imumsg);
              if (RAZOR.available()){
                while(RAZOR.available()>0){
                  RAZOR.read();
                }
              }
            }
            else{
              Serial.print(imumsg);
              BLUE.write(imumsg);
            }
          }
        }
      }
    }
  }
  digitalWrite(PIN_STATUS,LED_status);
  delay(150);
}

void writeString(String stringData) { // Used to serially push out a String with Serial.write()
  for (int i = 0; i < stringData.length(); i++)
  {
    BLUE.write(stringData[i]);   // Push each char 1 by 1 on each loop pass
  }

}
