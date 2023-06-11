package com.example.velmurugan.getcurrentlatitudeandlongitudeandroid;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Classe responsável por rastrear a localização GPS.
 */
class GpsTracker extends Thread implements LocationListener {
    private final Context mContext;
    private List<LocationData> LocationDataList; // Lista para armazenar as coordenadas

    // Sinalizador para status do GPS
    boolean isGPSEnabled = false;

    // Sinalizador para status da rede
    boolean isNetworkEnabled = false;

    Location location;
    double latitude;
    double longitude;

    // A distância mínima para alterar as atualizações em metros
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 metros

    // O tempo máximo para que ocorra atualizações
    private static final long MIN_TIME_BW_UPDATES = 1000 * 86400; // 1 dia

    // Declaração do gerenciador de localização
    protected LocationManager locationManager;

    /**
     * Construtor da classe GpsTracker.
     *
     * @param context o contexto da aplicação.
     */
    public GpsTracker(Context context) {
        this.mContext = context;
        LocationDataList = new ArrayList<>(); // Inicializa a lista de coordenadas
        getLocation();
    }

    /**
     * Obtém a localização atual.
     *
     * @return os dados de localização mais recentes.
     */
    public LocationData getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // Verifica o status do GPS
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Verifica o status da rede
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
            } else {
                if (isNetworkEnabled) {
                    // Verifica a permissão de rede
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            addLocationData(latitude, longitude); // Adiciona as coordenadas à lista
                        }
                    }
                }

                // Se o GPS estiver habilitado, obtem latitude/longitude usando os serviços de GPS
                if (isGPSEnabled) {
                    if (location == null) {
                        // Verifica a permissão de rede
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                addLocationData(latitude, longitude); // Adiciona as coordenadas à lista
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return LocationDataList.isEmpty() ? null : LocationDataList.get(LocationDataList.size() - 1);
    }

    /**
     * Adiciona as coordenadas à lista, juntamente com o timestamp.
     *
     * @param latitude  a latitude a ser adicionada.
     * @param longitude a longitude a ser adicionada.
     */
    private void addLocationData(double latitude, double longitude) {
        long timestamp = System.currentTimeMillis();
        LocationData LocationData = new LocationData(latitude, longitude, timestamp);
        LocationDataList.add(LocationData);

        // Ordena a lista de coordenadas pelo timestamp
        Collections.sort(LocationDataList, new Comparator<LocationData>() {
            @Override
            public int compare(LocationData c1, LocationData c2) {
                return Long.compare(c1.getTimestamp(), c2.getTimestamp());
            }
        });
    }

    /**
     * Obtém a lista de coordenadas ordenada pelo timestamp.
     *
     * @return a lista de coordenadas ordenada.
     */
    public List<LocationData> getSortedLocationData() {
        return LocationDataList;
    }

    /**
     * Método chamado quando a localização é alterada.
     *
     * @param location a nova localização.
     */
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            addLocationData(latitude, longitude); // Adiciona as coordenadas à lista
        }
    }

    /**
     * Método chamado quando o provedor de localização é desativado.
     *
     * @param provider o provedor que foi desativado.
     */
    @Override
    public void onProviderDisabled(String provider) {
        // Chamado quando o provedor é desabilitado pelo usuário
    }

    /**
     * Método chamado quando o provedor de localização é ativado.
     *
     * @param provider o provedor que foi ativado.
     */
    @Override
    public void onProviderEnabled(String provider) {
        // Chamado quando o provedor é habilitado pelo usuário
    }

    /**
     * Método chamado quando o status do provedor de localização muda.
     *
     * @param provider o provedor de localização.
     * @param status   o novo status.
     * @param extras   informações extras sobre o status.
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Chamado quando o status do provedor muda
    }
}