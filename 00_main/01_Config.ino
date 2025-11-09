// ====== VERSION ======
#define VERSION_FIRMWARE "2.2.5"

// ====== PINES ======
#define DHTPIN           4
#define DHTTYPE      DHT22
#define SUELO_PIN       34
#define ULTRASONIC_TRIG 12
#define ULTRASONIC_ECHO 33
#define CH1_IN          23
#define LED_R_PIN       25
#define LED_G_PIN       26
#define LED_B_PIN       27
#define FAN_CTRL_PIN    32
#define SERVO_PIN       13
#define SERVO_PIN2      14

// =================== CONSTANTES FÍSICAS ===================
const float SUELO_SECO        = 2600.0;  // valor de suelo seco
const float SUELO_MOJADO        = 800.0;  // valor de suelo mojado
const float DISTANCIA_MIN_CM = 3.0;   // tanque lleno
const float DISTANCIA_MAX_CM = 28.0;  // tanque vacío
const unsigned long TIEMPO_MAX_RIEGO = 5000; // 5 segundos

const unsigned long INTERVALO_SENSORES = 5000; 

#define DHTTYPE               DHT22      

// =================== VARIABLES DE ESTADO ===================
float nivelPct = 0.0;
float sueloPct =0.0;
float temperaturaActual = 0.0;
float humedadActual = 0.0;
float temperaturaObjetivo = 35.0;
float humedadObjetivo = 0.0;
bool modoUsuarioLED = false;

static unsigned long tInicioRiegoGlobal = 0;


// =================== TÓPICOS MQTT ===================
// Comandos
const char* T_BOMBA_CMD       = "invernadero/bomba/cmd";
const char* T_SUELO_HUM_CMD   = "invernadero/suelo/humedad/cmd";
const char* T_LED_CMD         = "invernadero/led/cmd";
const char* T_LED_POWER       = "invernadero/led/power";
const char* T_FAN_CMD         = "invernadero/ventiladores/cmd";
const char* T_OPTIMO_TEMP     = "invernadero/optimo/temperatura";
const char* T_OPTIMO_HUM      = "invernadero/optimo/humedad";
const char* T_SERVO_CMD       = "invernadero/servomotor1/cmd";

// Lecturas
const char* T_SUELO_HUM       = "invernadero/suelo/humedad";
const char* T_AIRE_TEMP       = "invernadero/aire/temperatura";
const char* T_AIRE_HUM        = "invernadero/aire/humedad";
const char* T_TANQUE_NIVEL    = "invernadero/tanque/nivel";

// Alertas y notificaciones
const char* T_ALERTAS         = "invernadero/alertas";
const char* T_NOTIFICACIONES  = "invernadero/notificaciones";

