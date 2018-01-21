package com.surf.dsasm;

import com.binance.api.client.domain.market.CandlestickInterval;

class ExponentialMovingAverage {
    private double alpha;
    private Double oldValue;
    public ExponentialMovingAverage(CandlestickInterval timePeriod) {
        this.alpha = (2 / (CandlestickIntervalUtils.timeInMinutes(timePeriod) + 1) ) ;
    }

    public double average(double value) {
        if (oldValue == null) {
            oldValue = value;
            return value;
        }
        double newValue = oldValue + alpha * (value - oldValue);
        oldValue = newValue;
        return newValue;
    }
}