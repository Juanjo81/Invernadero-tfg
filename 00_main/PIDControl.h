#include <Arduino.h>
#ifndef PIDCONTROL_H
#define PIDCONTROL_H

#include <PubSubClient.h>
#define PID_MIN -100.0
#define PID_MAX 100.0

struct PIDControl {
  float Kp, Ki, Kd;
  float setpoint = 0.0;
  float input = 0.0;
  float output = 0.0;
  float errorAcumulado = 0.0;
  float errorAnterior = 0.0;
  unsigned long tAnterior = 0;

  void configurar(float kp, float ki, float kd) {
    Kp = kp; Ki = ki; Kd = kd;
    tAnterior = millis();
  }

  void actualizar(float nuevaEntrada, float nuevoSetpoint) {
    input = nuevaEntrada;
    setpoint = nuevoSetpoint;

    float error = setpoint - input;
    unsigned long ahora = millis();
    float deltaT = (ahora - tAnterior) / 1000.0;
    tAnterior = ahora;

    // Protección contra deltaT cero o negativo
    if (deltaT <= 0.0) deltaT = 0.001;

    // Acumulación limitada para evitar saturación
    errorAcumulado += error * deltaT;
    errorAcumulado = constrain(errorAcumulado, PID_MIN, PID_MAX);

    // Derivada 
    float derivada = (error - errorAnterior) / deltaT;
    errorAnterior = error;

    // Cálculo de salida sin saturación interna
    float salida = Kp * error + Ki * errorAcumulado + Kd * derivada;
    output = salida; 
  }
};

extern PIDControl pidTemp;
extern PIDControl pidHum;

#endif