package tja.capability.handler;

public interface IBoilerHandler extends IMachineHandler {

    double getHeatEfficiencyMultiplier();

    double getFuelConsumptionMultiplier();

    int getBaseSteamOutput();

    int getMaxTemperature();
}
