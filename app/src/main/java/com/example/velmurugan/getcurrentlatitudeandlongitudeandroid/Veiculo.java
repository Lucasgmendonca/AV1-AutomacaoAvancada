package com.example.velmurugan.getcurrentlatitudeandlongitudeandroid;

import java.util.List;

/**
 * Classe que representa um veículo.
 */
class Veiculo extends Thread {
    private final GpsTracker gpsTracker;
    private boolean verificaTrocaLocalizacao;
    private double velocidadeMediaParcial;
    private double distanciaPercorrida;
    private double consumoCombustivelTotal;
    private double velocidadeMediaTotal;
    private double velocidadeRecomendada;
    private long tempoDeslocamento;
    private long tempoParaDestinoFinal;
    private int intervaloLocalizacoes;

    private static final double LATITUDE_FINAL = -20.4569;
    private static final double LATITUDE_INICIAL = -45.8358;
    private static final long TEMPO_PARA_DESTINO_FINAL = 100;
    private static final double DESLOCAMENTO_TOTAL = 2.598461;
    private static final double DESLOCAMENTO_PARCIAL = 0.5052034858527461;

    /**
     * Construtor da classe Veiculo.
     *
     * @param gpsTracker o objeto GpsTracker responsável por rastrear a localização do veículo.
     */
    public Veiculo(GpsTracker gpsTracker) {
        this.gpsTracker = gpsTracker;
        this.verificaTrocaLocalizacao = false;
        this.velocidadeMediaParcial = 0;
        this.distanciaPercorrida = 0;
        this.consumoCombustivelTotal = 0;
        this.velocidadeMediaTotal = 0;
        this.velocidadeRecomendada = 0;
        this.tempoDeslocamento = 0;
        this.tempoParaDestinoFinal = TEMPO_PARA_DESTINO_FINAL;
        this.intervaloLocalizacoes = 1;
    }

    /**
     * Obtém o valor da variável que indica se houve troca de localização.
     *
     * @return true se houve troca de localização, false caso contrário.
     */
    public boolean getVerificaTrocaLocalizacao() {
        return verificaTrocaLocalizacao;
    }

    /**
     * Obtém a velocidade média parcial do veículo.
     *
     * @return a velocidade média parcial.
     */
    public double getVelocidadeMediaParcial() {
        return velocidadeMediaParcial;
    }

    /**
     * Obtém a distância percorrida pelo veículo.
     *
     * @return a distância percorrida.
     */
    public double getDistanciaPercorrida() {
        return distanciaPercorrida;
    }

    /**
     * Obtém o consumo total de combustível do veículo.
     *
     * @return o consumo total de combustível.
     */
    public double getConsumoCombustivelTotal() {
        return consumoCombustivelTotal;
    }

    /**
     * Obtém a velocidade média total do veículo.
     *
     * @return a velocidade média total.
     */
    public double getVelocidadeMediaTotal() {
        return velocidadeMediaTotal;
    }

    /**
     * Obtém a velocidade recomendada para o veículo.
     *
     * @return a velocidade recomendada.
     */
    public double getVelocidadeRecomendada() {
        return velocidadeRecomendada;
    }

    /**
     * Obtém o tempo de deslocamento do veículo.
     *
     * @return o tempo de deslocamento.
     */
    public long getTempoDeslocamento() {
        return tempoDeslocamento;
    }

    /**
     * Obtém o tempo restante para o destino final do veículo.
     *
     * @return o tempo restante para o destino final.
     */
    public long getTempoParaDestinoFinal() {
        return tempoParaDestinoFinal;
    }

    /**
     * Obtém o intervalo de localizações do veículo.
     *
     * @return o intervalo de localizações.
     */
    public int getIntervaloLocalizacoes() {
        return intervaloLocalizacoes;
    }

    /**
     * Calcula a distância entre duas coordenadas geográficas.
     *
     * @param lat1 a latitude da primeira coordenada.
     * @param lon1 a longitude da primeira coordenada.
     * @param lat2 a latitude da segunda coordenada.
     * @param lon2 a longitude da segunda coordenada.
     * @return a distância entre as coordenadas em quilômetros.
     */
    private double calculoDistancia(double lat1, double lon1, double lat2, double lon2) {
        double raioTerra = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return raioTerra * c;
    }

    /**
     * Calcula o consumo de combustível com base na velocidade média parcial.
     *
     * @return o consumo de combustível.
     */
    private double calculoConsumoCombustivel() {
        double consumoCombustivelPorKm;

        if (getVelocidadeMediaParcial() >= 0 && getVelocidadeMediaParcial() <= 80) {
            consumoCombustivelPorKm = 0.047;
        } else if (getVelocidadeMediaParcial() > 80 && getVelocidadeMediaParcial() <= 120) {
            consumoCombustivelPorKm = 0.0641;
        } else {
            consumoCombustivelPorKm = 0.0962;
        }

        return DESLOCAMENTO_PARCIAL * consumoCombustivelPorKm;
    }

    /**
     * Atualiza os dados do veículo com a nova localização.
     *
     * @param latitude  a nova latitude.
     * @param longitude a nova longitude.
     * @param timestamp o timestamp da nova localização.
     */
    public void atualizarDados(double latitude, double longitude, long timestamp) {
        List<LocationData> locationDataList = gpsTracker.getSortedLocationData();
        int size = locationDataList.size();

        if (size >= 1) {
            LocationData lastLocationData = locationDataList.get(size - 1);

            if (lastLocationData.getLatitude() != latitude || lastLocationData.getLongitude() != longitude) {
                lastLocationData.setLatitude(latitude);
                lastLocationData.setLongitude(longitude);
                lastLocationData.setTimestamp(timestamp);
                verificaTrocaLocalizacao = true;
            }

            if (size >= 2) {
                LocationData secondLastLocationData = locationDataList.get(size - 2);

                double distanciaTotal = calculoDistancia(
                        secondLastLocationData.getLatitude(), secondLastLocationData.getLongitude(),
                        latitude, longitude
                );

                distanciaPercorrida = distanciaTotal;

                if (latitude < LATITUDE_FINAL && longitude > LATITUDE_INICIAL) {
                    tempoDeslocamento = (System.currentTimeMillis() - secondLastLocationData.getTimestamp()) / 1000;
                    tempoParaDestinoFinal--;

                    if (getTempoDeslocamento() <= 1) {
                        velocidadeRecomendada = (DESLOCAMENTO_TOTAL * 1000 / TEMPO_PARA_DESTINO_FINAL) * 3.6;
                    }
                }

                if (getVerificaTrocaLocalizacao()) {
                    velocidadeMediaParcial = (DESLOCAMENTO_PARCIAL * 1000 / getIntervaloLocalizacoes()) * 3.6;
                    consumoCombustivelTotal += calculoConsumoCombustivel();
                    velocidadeMediaTotal = (getDistanciaPercorrida() * 1000 / getTempoDeslocamento()) * 3.6;
                    intervaloLocalizacoes = 1;
                    velocidadeRecomendada = ((DESLOCAMENTO_TOTAL - distanciaTotal) * 1000 / getTempoParaDestinoFinal()) * 3.6;
                }

                verificaTrocaLocalizacao = false;
                intervaloLocalizacoes++;
            }
        }
    }
}