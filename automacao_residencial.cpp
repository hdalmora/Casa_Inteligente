#include "mbed.h"
#include <string>
#include "mbed.h"
#include "MFRC522.h"
#include "TextLCD.h"

// Definicão dos pinos 
//modulo RFID MFRC522
#define MF_RESET    D8
#define SPI_MOSI    D11
#define SPI_MISO    D12
#define SPI_SCK     D13
#define SPI_CS      D10

//Pinos UART0 Para comunicação serial do módulo Bluetooth
#define UART2_TX    PTA2
#define UART2_RX    PTA1


//Comunicacão serial USB para print de logs no PC   
Serial pc(USBTX, USBRX);

//Inicialização do bluetooth e RFID
Serial bluetooth(UART2_TX, UART2_RX);
MFRC522 RfChip (SPI_MOSI, SPI_MISO, SPI_SCK, SPI_CS, MF_RESET);

// LCD 16x2
TextLCD lcd(PTE22, PTE23, PTE2, PTE3, PTE4, PTE5);

//Componentes utilizado no projeto
PwmOut led(PTA4);  //Led que será controlado pelo aplicativo
PwmOut led_alarme(PTA5); //Led vermelho para o sistema de alarme
DigitalOut buzzer(PTA17); //Buzzer para o sistema de alarme
AnalogIn LM35(PTB0); //Sensor de temperatura

//Strings (Comandos) específicos que serão recebidos pela comunicação bluetooth
string dadosRecebidos = "";
string setLedOn = "N";
string setLedOff = "E";
string setAlarmeOff = "K";
string sendAlarmAlert = "ALARME";

//Variáveis de armazenamento
float temperaturaCelsius; // Variável que armazenará a temperatura medida
float fatorDeLeitura = 0.5f; //Para atualizar a temperatura, esta deve ser maior 
//ou menor conforme o valor de fatorDeLeitura em relação a leitura anterior
float pwmValue; //Valor pwm recebido pelo aplicativo para controle do led
int contadorAcesso = 0; //Controle de acesso RFID
char tentativas_char[50]; //Armazena o numero de tentativas como char array

//Variáveis de controle
bool ledOn = false;
bool alarmeAtivado = false;
bool buzzerAlarmOn = false;
bool pwmRange = (pwmValue >= 0 && pwmValue <= 1);
bool tag_encontrada = false;


//Armazenamento da TAG lida pelo módulo RFID
char uid[50];
string cardUID = "";

//ARRAY contendo as TAGS cadastradas com controle de acesso no sistema
#define TAMANHO_ARRAY_TAGS 2
const string rfidTagsRegistradas[TAMANHO_ARRAY_TAGS] = {
     "B902 2502 A002 1A02 ",
     "0000 0000 0000 0000 ",
};


/* ***************************************************************** */
/* ------------------------- -  FUNÇÔES -  ------------------------- */
void escreverLCD(string linha1, string linha2) {
    lcd.cls();
    if (linha1 != "null") {
        lcd.locate(0,0);
        lcd.printf(linha1.c_str());
    }
    
    if (linha2 != "null") {
        lcd.locate(0,1);
        lcd.printf(linha2.c_str());
    }
    
    //(tempo_delay_ms > 0) ? wait_ms(tempo_delay_ms) : wait_ms(0);
}

void enviarDadosBluetooth(string data, float temp) { 
    if(data.length() > 0 && data != "null") {
        bluetooth.printf("%s\n", data.c_str());
    } 
    if (temp != -1){
        bluetooth.printf("T%.2f\n", temp); 
    }
    wait(0.05);
}

void checarTemperatura(int duracao_atraso_ms) {
    float leituraAtual = LM35.read() * 3.685503686 * 100;
    if(temperaturaCelsius != 0) {
        if (leituraAtual > (temperaturaCelsius+fatorDeLeitura) || 
            leituraAtual < (temperaturaCelsius-fatorDeLeitura)) 
        {
            temperaturaCelsius = leituraAtual;
            enviarDadosBluetooth("null", temperaturaCelsius);
        }
    } else {
        temperaturaCelsius = leituraAtual;
        enviarDadosBluetooth("null", temperaturaCelsius);
    }
    if(duracao_atraso_ms > 0) {
        wait_ms(duracao_atraso_ms);   
    }
}

float ledPWM(string pwm) {
    pc.printf("PWM: %s\n", pwm.c_str());
    return (atof(pwm.c_str())/10.0f);
}

void desligarAlarmeSonoro() {
    buzzer = 0;
    escreverLCD("Alarme desligado", "null");
    wait_ms(500);
    escreverLCD("Casa Inteligente", "null");
}

void buzeerBeep(int duracao_ms) {
  buzzer = 1;
  wait_ms(duracao_ms); 
  buzzer = 0;
}

void ligarAlarmeSonoro() {
    buzzer = 1;
    wait_ms(500);
    buzzer = 0;
    wait_ms(50);
}

string dadosBluetoothRecebidos(){
    string data = "";
    
    if (bluetooth.readable()) {
        while(bluetooth.readable() > 0) {
            data += (char)bluetooth.getc();
            wait(0.05);
        }
        //pc.printf("String Recebida: ");
        pc.printf("Recebido: %s\n", data.c_str());
        return data;
    } else {
        return "null";
    }
}

void estadoComponentes(string dado) {
    if (dado == setLedOn) {
        pc.printf("SetLedOn\n");
        ledOn = true;
        if(pwmRange) {
            pc.printf("SetLedOn - PwmRange OK, Value: %f\n", pwmValue);
            led = 0 + pwmValue;
            wait(0.05);  
        }
    } else if (dado == setLedOff) {
        pc.printf("SetLedOFF");
        led = 0;
        ledOn = false;
    } else if (dado == setAlarmeOff) {
        alarmeAtivado = false;
        led_alarme = 0; //Desligar Led Vermelho do Alarme
        if(buzzerAlarmOn) {
            buzzerAlarmOn = false;
            desligarAlarmeSonoro();
        }
    } else {
        //pc.printf("%s", "Valor PWM Led: ");
        //pc.printf("%s\n", dado.c_str());
        pc.printf("PWM CHANGE: %s\n", dado.c_str());
        pwmValue = ledPWM(dado);
        pc.printf("PWM VALUE: %f\n", pwmValue);
        
        if(pwmRange) {
            if(ledOn) {
                led = 0 + pwmValue;
                wait(0.05);    
            }   
        } else {
            pc.printf("%s", "Ocorreu um erro, O valor PWM nao esta entre 0 e 1");
        }
    }   
}

string leituraRFID() {
    if (RfChip.PICC_IsNewCardPresent()) {
        wait_ms(250);
        if(RfChip.PICC_ReadCardSerial()) {
            cardUID = "";
            //pcrintf("Card UID: ");
            for (uint8_t i = 0; i < RfChip.uid.size; i++)
            {
                sprintf(uid, "%X02 ", RfChip.uid.uidByte[i]);
                cardUID += uid;
            }
            return cardUID;
        } else {
            return "null";  
        }
    } else {
        return "null";  
    }
}

// ------------------------------------------------------------------------ //


/* ************************************************************************* */
/* --------------------------------------   FUNÇÂO MAIN   ----------------- */
int main() 
{
    lcd.cls();
    pc.baud(9600);
    bluetooth.baud(9600);
    // Iniciar o módulo RFID
    RfChip.PCD_Init();

    escreverLCD("Casa Inteligente", "Iniciando...");
    wait_ms(1500);
    escreverLCD("Casa Inteligente", "null");
    
    while(1) {
        /* ********* Rotina de dados Recebidos pelo Bluetooth  ********* */
        /* ************** (Controle dos Componentes da Casa) *********** */
        dadosRecebidos = dadosBluetoothRecebidos();
        if(dadosRecebidos != "null") {
            estadoComponentes(dadosRecebidos);    
            dadosRecebidos = ""; //Limpar a string para o próximo comando
        }
        // --------------------------------------------------------------- //
      
      
        /* ********* Rotina de dados Enviados para o Bluetooth  ********** */
        /**** (Controle de Acesso (RFID), Alarme da Casa e Temperatura) ** */
        if (!alarmeAtivado) {
            string tag = leituraRFID();
            if (tag != "null") {
                for(uint8_t i = 0; i < TAMANHO_ARRAY_TAGS; i++) {
                    if (tag == rfidTagsRegistradas[i]) {
                        escreverLCD("Acesso liberado !", "Bem vindo");
                        buzeerBeep(100);
                        tag_encontrada = true;   
                        wait_ms(1000);
                        escreverLCD("Casa Inteligente", "null");
                    }
                }
                wait(0.02);
                if(!tag_encontrada) {
                    escreverLCD("Acesso negado !", "null");
                    wait_ms(400);
                    contadorAcesso += 1;
                    if(contadorAcesso >= 3) {
                        //Ativar o Alarme
                        enviarDadosBluetooth(sendAlarmAlert, -1);
                        alarmeAtivado = true;
                        buzzerAlarmOn = true;
                        led_alarme = 1; //Led Vermelho do Alarme
                        contadorAcesso = 0;
                        escreverLCD("Alarme Ativado.", "Entrada bloqueada");
                    } else {
                        string tentativas_str = "";
                        int tentativas = 3 - contadorAcesso;
                        
                        sprintf(tentativas_char, "%i ", tentativas);
                        tentativas_str += tentativas_char;
                        tentativas_str += " tentativas";
                        
                        escreverLCD("Voce possui mais", tentativas_str);
                        buzeerBeep(300);
                        wait(1); 
                        escreverLCD("Casa Inteligente", "null");    
                    }
                    wait(0.2);
                } else {
                    tag_encontrada = false;
                }
            } 
            checarTemperatura(500);
        } else {
            //Alarme esta ativado, bloquear entrada e acionar o buzzer
            ligarAlarmeSonoro(); 
            checarTemperatura(1000);
        }
        // ------------------------------------------------------ //
        
    } //fim while(1)
}//fim main()
/* ***************************************************************** */
// ------------------------------------------------------------------ //