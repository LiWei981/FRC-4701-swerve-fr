package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.SparkMaxLimitSwitch.Type;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import entech.subsystems.EntechSubsystem;
import frc.robot.RobotConstants;
import frc.robot.RobotConstants.ELBOW;
import frc.robot.controllers.PositionControllerConfig;
import frc.robot.controllers.SparkMaxPositionController;

public class ElbowSubsystem extends EntechSubsystem {

    private static final double NUDGE_COUNT = 1.5;
    private CANSparkMax elbowMotor;
    private SparkMaxPositionController positionController;

    private boolean enabled = false;
    private double position = ELBOW.POSITION_PRESETS.MIN_POSITION_DEGREES;

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        if (this.positionController != null) {
            this.positionController.requestPosition(position);
        }
        this.position = position;
    }

    public void homePosition() {
        setPosition(ELBOW.POSITION_PRESETS.MIN_POSITION_DEGREES);
    }

    // for unit testing
    public ElbowSubsystem(CANSparkMax motor, SparkMaxPositionController controller) {
        this.enabled = true;
        this.elbowMotor = motor;
        this.positionController = controller;

    }

    // for match
    public ElbowSubsystem() {

    }

    public void clearRequestedPosition() {
        positionController.clearRequestedPosition();
    }

    public boolean isHomed() {
        if (positionController != null) {
            return positionController.isHomed();
        } else {
            return false;
        }
    }

    @Override
    public void initialize() {
        if (enabled) {
            elbowMotor = new CANSparkMax(RobotConstants.Ports.CAN.ELBOW_MOTOR_ID, MotorType.kBrushless);
            elbowMotor.getPIDController().setP(ELBOW.TUNING.P_GAIN);
            elbowMotor.getPIDController().setI(ELBOW.TUNING.I_GAIN);
            elbowMotor.getPIDController().setD(ELBOW.TUNING.D_GAIN);
            elbowMotor.setSmartCurrentLimit(ELBOW.SETTINGS.MAX_SPIKE_CURRENT);

            elbowMotor.set(0);
            elbowMotor.setIdleMode(IdleMode.kBrake);
            elbowMotor.clearFaults();
            elbowMotor.setInverted(ELBOW.SETTINGS.MOTOR_REVERSED);
            elbowMotor.getEncoder().setPositionConversionFactor(ELBOW.SETTINGS.COUNTS_PER_DEGREE);
            elbowMotor.getEncoder().setVelocityConversionFactor(ELBOW.SETTINGS.COUNTS_PER_DEGREE);
            PositionControllerConfig conf = new PositionControllerConfig.Builder("ELBOW")
                    .withHomingOptions(ELBOW.HOMING.HOMING_SPEED_PERCENT)
                    // .withHomingVelocity(ELBOW.HOMING.HOMING_SPEED_VELOCITY)
                    .withPositionTolerance(ELBOW.SETTINGS.MOVE_TOLERANCE_DEGREES)
                    .withSoftLimits(ELBOW.POSITION_PRESETS.MIN_POSITION_DEGREES,
                            ELBOW.POSITION_PRESETS.MAX_POSITION_DEGREES)
                    .withHomeAtCurrentAmps(ELBOW.HOMING.HOMING_CURRENT_AMPS)
                    .withInverted(true)
                    .build();

            positionController = new SparkMaxPositionController(
                    elbowMotor,
                    conf,
                    elbowMotor.getReverseLimitSwitch(Type.kNormallyOpen),
                    elbowMotor.getForwardLimitSwitch(Type.kNormallyOpen),
                    elbowMotor.getEncoder());
        }
    }

    public boolean isAtLowerLimit() {
        return positionController.isAtLowerLimit();
    }

    public void home() {
        positionController.home();
    }

    public boolean isSafeToExtendArm() {
        return this.getActualPosition() > ELBOW.POSITION_PRESETS.SAFE_ANGLE;
    }

    public void setMotorSpeed(double speed) {
        positionController.setMotorSpeed(speed);
    }

    public double getRequestedPosition() {
        if (isEnabled()) {
            return positionController.getRequestedPosition();
        } else {
            return RobotConstants.INDICATOR_VALUES.POSITION_UNKNOWN;
        }
    }

    public double getActualPosition() {
        if (isEnabled()) {
            return positionController.getActualPosition();
        } else {
            return RobotConstants.INDICATOR_VALUES.POSITION_UNKNOWN;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public SparkMaxPositionController getPositionController() {
        return positionController;
    }

    public void requestPosition(double requestedPosition) {
        DriverStation.reportWarning("Elbow Angle Request:" + requestedPosition, false);
        positionController.requestPosition(requestedPosition);
    }

    public void restorePosition() {
        positionController.setPositionMode();
    }

    public void stop() {
        positionController.stop();
    }

    public boolean isAtRequestedPosition() {
        return positionController.isAtRequestedPosition();
    }

    public void periodic() {
        if (enabled) {
            positionController.update();

            SmartDashboard.putBoolean("Elbow Upper Limit", positionController.isAtUpperLimit());
            SmartDashboard.putBoolean("Elbow Lower Limit", positionController.isAtLowerLimit());
            SmartDashboard.putNumber("Elbow Position", positionController.getActualPosition());
            SmartDashboard.putBoolean("Elbow Homed", isHomed());
            SmartDashboard.putString("Elbow State", positionController.getStatusString());
            // if (getCurrentCommand() != null)
            // SmartDashboard.putString("Elbow Command", getCurrentCommand().toString());
            SmartDashboard.putNumber("Elbow Req Position", positionController.getRequestedPosition());
        }
    }

    // @Override
    // public void initSendable(SendableBuilder builder) {
    // super.initSendable(builder);
    // builder.setSmartDashboardType(getName());
    // builder.addDoubleProperty("Position", this::getPosition, this::setPosition);
    // if ( enabled ) {
    // //builder.addBooleanProperty("AtSetPoint", this::isAtRequestedPosition,
    // null);
    // //builder.addDoubleProperty("RequestedPos", this::getRequestedPosition,
    // null);
    // //builder.addDoubleProperty("ActualPos", this::getActualPosition, null);
    // //builder.addBooleanProperty("CanExtendArm", this::isSafeToExtendArm, null);
    // }
    // }

    @Override
    public void simulationPeriodic() {

    }

    public void nudgeElbowDown() {
        positionController.requestPosition(getActualPosition() - NUDGE_COUNT);
    }

    public void nudgeElbowUp() {
        positionController.requestPosition(getActualPosition() + NUDGE_COUNT);
    }

    public void forgetHome() {
        positionController.forgetHome();
    }

}
