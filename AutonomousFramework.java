package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
/**
 * AutonomousFramework 1.0
 * Created by CCA on 6/28/2016.
 * ToDo
 * rightForTime, backwardForTime commands
 * left,right,forward,backwardForDistance commands
 * smoothLeftForDistance() etc commands
 * implement IF, WHILE, FOR
 */


public class AutonomousFramework extends OpMode {
    DcMotor motorRight;
    DcMotor motorLeft;

    enum State {INIT, RUNNING, DONE}
    State state;

    public ElapsedTime runTime = new ElapsedTime();
    double timeMark = 0;

    int programCounter = 0;



    public interface Command {

        State start();

        State run();

        State stop();

    }

    public double motorEncAvg() {
        return ((motorRight.getCurrentPosition() + motorLeft.getCurrentPosition())/2);
    }

    public class forwardForTime implements Command {

        private double power, time, target;

        public forwardForTime(double myPower, double myTime) {
            power = Range.clip(myPower,0,1);
            time = Range.clip(myTime,0,30);  // 30 seconds is max autonomous time period

        }

        public State start() {
            state = State.RUNNING;
            target = runTime.time() + time;
            return state;
        }

        public State run() {
            if (runTime.time() < target) {
                motorLeft.setPower(power);
                motorRight.setPower(power);
                return state;
            }
            else {
                state = State.DONE;
                return state;
            }
        }

        public State stop() {
            return State.DONE;
        }
    }

    public class forwardForDistance implements Command {
        private double power, clicks, target;

        public forwardForDistance(double myPower, double myClicks) {
            power = Range.clip(myPower, 0, 1);
            clicks = Range.clip(myClicks, 0, 999999999);
        }

        public State start() {
            state = State.RUNNING;
            target = motorEncAvg() + clicks;
            return state;
        }

        public State run() {
            telemetry.addData("EncAvg:", String.format("%.2f", motorEncAvg()));

            if (motorEncAvg() < target) {
                motorLeft.setPower(power);
                motorRight.setPower(power);
                return state;
            }
            else {
                state = State.DONE;
                return state;
            }
        }

        public State stop() {
            return State.DONE;
        }
    }

    public class backwardForTime implements Command {

        private double power, time, target;

        public backwardForTime(double myPower, double myTime) {
            power = Range.clip(myPower,0,1);
            time = Range.clip(myTime,0,30);  // 30 seconds is max autonomous time period

        }

        public State start() {
            state = State.RUNNING;
            target = runTime.time() + time;
            return state;
        }

        public State run() {
            if (runTime.time() < target) {
                motorLeft.setPower(-power);
                motorRight.setPower(-power);
                return state;
            }
            else {
                state = State.DONE;
                return state;
            }
        }

        public State stop() {
            return State.DONE;
        }
    }

    public class leftForTime implements Command {
        double power, time, target;

        public leftForTime(double myPower, double myTime) {
            power = Range.clip(myPower,0,1);
            time = Range.clip(myTime,0,30);  // 30 seconds is max autonomous time period
        }

        public State start() {
            state = State.RUNNING;
            target = runTime.time() + time;
            return state;

        }

        public State run() {
            if (runTime.time() < target) {
                motorLeft.setPower(-power);
                motorRight.setPower(power);
                return state;
            }
            else {
                state = State.DONE;
                return state;
            }
        }

        public State stop() {
            return State.DONE;
        }
    }

    public class rightForTime implements Command {
        double power, time, target;

        public rightForTime(double myPower, double myTime) {
            power = Range.clip(myPower,0,1);
            time = Range.clip(myTime,0,30);
        }

        public State start() {
            state = State.RUNNING;
            target = runTime.time() + time;
            return state;
        }

        public State run() {
            if (runTime.time() < target) {
                motorLeft.setPower(power);
                motorRight.setPower(-power);
                return state;
            } else {
                state = State.DONE;
                return state;
            }
        }

            public State stop() { return State.DONE; }
        }

    public class stopForTime implements Command {
        double time, target;

        public stopForTime(double myTime) {
            time = Range.clip(myTime, 0, 30); // 30 seconds is max autonomous time period
        }

        public State start() {
            state = State.RUNNING;
            target = runTime.time() + time;
            return state;
        }

        public State run() {
            if (runTime.time() < target) {
                motorLeft.setPower(0);
                motorRight.setPower(0);
                return state;
            }
            else {
                state = State.DONE;
                return state;
            }
        }

        public State stop() {
            return State.DONE;
        }

    }

    Command[] commandList = {new forwardForDistance(.5, 1500),
                            new stopForTime(0.5)};
    Command currentCommand;

    public void init(){
        motorRight = hardwareMap.dcMotor.get("motorRight");
        motorLeft = hardwareMap.dcMotor.get("motorLeft");

        motorLeft.setDirection(DcMotor.Direction.FORWARD);
        motorRight.setDirection(DcMotor.Direction.REVERSE);

        state = State.INIT;
        programCounter = 0;
        currentCommand = commandList[programCounter];
    }

    public void loop() {
        switch(state) {
            case DONE: {
                currentCommand.stop();
                programCounter += 1;
                if (programCounter < commandList.length) {
                    currentCommand = commandList[programCounter];
                    state = State.INIT;
                }
                else {
                    stop();
                }
            }
            case INIT: {
                state = currentCommand.start();
            }
            case RUNNING: {
                state = currentCommand.run();
            }
            default: {
                stop();
            }

        }
    }


}
