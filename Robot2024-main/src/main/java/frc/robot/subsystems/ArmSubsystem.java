package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.SparkMaxLimitSwitch.Type;
import com.revrobotics.SparkMaxPIDController;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import entech.subsystems.EntechSubsystem;
import frc.robot.RobotConstants;
import frc.robot.RobotConstants.ARM;
import frc.robot.RobotConstants.ARM.TUNING;
import frc.robot.controllers.PositionControllerConfig;
import frc.robot.controllers.SparkMaxPositionController;

/**
 *
 * @author dcowden
 */

 public class ArmSubsystem extends EntechSubsystem {

    private static final double NUDGE_COUNT = 0.05;
    private CANSparkMax telescopeMotor;
    private SparkMaxPositionController positionController;
    private boolean enabled = false;

    private double position = ARM.POSITION_PRESETS.MIN_METERS;

    public double getPosition() {
        return position;
    }

    public void homePosition() {
        setPosition(ARM.POSITION_PRESETS.MIN_METERS);
    }

    public void setPosition(double position) {
        if (this.positionController != null) {
            this.positionController.requestPosition(position);
        }
        this.position = position;
    }

    // for unit testing
    public ArmSubsystem(CANSparkMax motor, SparkMaxPositionController controller) {
        this.enabled = true;
        this.telescopeMotor = motor;
        //this.positionController = controller;
        //temporary fix
        //this.positionController = ;

    }

    public SparkMaxPositionController getPositionController() {
        return positionController;
    }

    // for match
    public ArmSubsystem() {
    }

    @Override
    public void initialize() {
        if (enabled) {
            telescopeMotor = new CANSparkMax(RobotConstants.Ports.CAN.TELESCOPE_MOTOR_ID, MotorType.kBrushless);
            SparkMaxPIDController pid = telescopeMotor.getPIDController();
            pid.setP(TUNING.P_GAIN);
            pid.setI(TUNING.I_GAIN);
            pid.setD(TUNING.D_GAIN);
            pid.setOutputRange(-1.0, 1.0);

            telescopeMotor.set(0);
            telescopeMotor.setIdleMode(IdleMode.kBrake);
            telescopeMotor.clearFaults();
            telescopeMotor.setInverted(ARM.SETTINGS.MOTOR_REVERSED);
            telescopeMotor.getEncoder().setPositionConversionFactor(ARM.SETTINGS.COUNTS_PER_METER);
            telescopeMotor.getEncoder().setVelocityConversionFactor(ARM.SETTINGS.COUNTS_PER_METER);
            PositionControllerConfig conf = new PositionControllerConfig.Builder("ARM")
                    .withSoftLimits(ARM.POSITION_PRESETS.MIN_METERS, ARM.POSITION_PRESETS.MAX_METERS)
                    .withHomingOptions(ARM.HOMING.HOMING_SPEED_PERCENT)
                    // .withHomingVelocity(ARM.HOMING.HOMING_SPEED_VELOCITY)
                    .withPositionTolerance(ARM.SETTINGS.MOVE_TOLERANCE_METERS)
                    .withHomeAtCurrentAmps(ARM.HOMING.HOMING_CURRENT_AMPS)
                    .withInverted(true)
                    .build();

            positionController = new SparkMaxPositionController(
                    telescopeMotor,
                    conf,
                    telescopeMotor.getReverseLimitSwitch(Type.kNormallyOpen),
                    telescopeMotor.getForwardLimitSwitch(Type.kNormallyOpen),
                    telescopeMotor.getEncoder());
        }
    }

    public void setMotorSpeed(double speed) {
        positionController.setMotorSpeed(speed);
    }

    public void requestPosition(double requestedPosition) {
        positionController.requestPosition(requestedPosition);
    }

    public void clearRequestedPosition() {
        positionController.clearRequestedPosition();
    }

    public void home() {
        positionController.home();
    }

    public void stop() {
        positionController.stop();
    }

    public boolean isAtRequestedPosition() {
        return positionController.isAtRequestedPosition();
    }

    public double getRequestedPosition() {
        if (isEnabled()) {
            return positionController.getRequestedPosition();
        } else {
            return RobotConstants.INDICATOR_VALUES.POSITION_UNKNOWN;
        }
    }

    public boolean isHomed() {
        if (isEnabled()) {
            return positionController.isHomed();
        } else {
            return false;
        }
    }

    public double getActualPosition() {
        if (isEnabled()) {
            return positionController.getActualPosition();
        } else {
            return RobotConstants.INDICATOR_VALUES.POSITION_UNKNOWN;
        }
    }

    public void nudgeArmForward() {
        positionController.requestPosition(getActualPosition() + NUDGE_COUNT);
    }

    public void nudgeArmBackwards() {
        positionController.requestPosition(getActualPosition() - NUDGE_COUNT);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void periodic() {
        if (enabled) {
            positionController.update();

            SmartDashboard.putBoolean("Arm Upper Limit", positionController.isAtUpperLimit());
            SmartDashboard.putBoolean("Arm Lower Limit", positionController.isAtLowerLimit());
            SmartDashboard.putNumber("Arm Position", positionController.getActualPosition());
            SmartDashboard.putBoolean("Arm Homed", isHomed());
            SmartDashboard.putString("Arm State", positionController.getStatusString());
            // if (getCurrentCommand() != null)
            // SmartDashboard.putString("Arm Command", getCurrentCommand().toString());
            SmartDashboard.putNumber("Arm Req Position", positionController.getRequestedPosition());
        }
    }

    public boolean isArmRetracted() {
        return positionController.getActualPosition() < ARM.POSITION_PRESETS.SAFE;
    }

    public boolean isAtLowerLimit() {
        return positionController.isAtLowerLimit();
    }

    // @Override
    // public void initSendable(SendableBuilder builder) {
    //
    // super.initSendable(builder);
    // builder.setSmartDashboardType(getName());
    // builder.addDoubleProperty("Position", this::getPosition, this::setPosition);
    // if ( enabled ) {
    // //builder.addBooleanProperty("AtSetPoint", this::isAtRequestedPosition,
    // null);
    // //builder.addDoubleProperty("RequestedPos", this::getRequestedPosition,
    // null);
    // //builder.addDoubleProperty("ActualPos", this::getActualPosition, null);
    // }
    // }

    @Override
    public void simulationPeriodic() {

    }

    public void forgetHome() {
        positionController.forgetHome();
    } 
}
