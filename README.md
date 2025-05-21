# ENSEA_Java_Final

## Project description
Make a circle focused physics simulator.

## Instructions
- Windows
    - Ensure `<properties>` of `pom.xml` contains `<lwjgl.natives>natives-windows</lwjgl.natives>` to ensure the correct lwjgl libraries are downloaded in the compilation process
    - Compile the project with `mvn clean compile`
    - The project is executed with the plugin `exec`; therefore execute the project with `mvn exec:java`
- Mac
    - Ensure `<properties>` of `pom.xml` contains `<lwjgl.natives>natives-macos-arm64</lwjgl.natives>` to ensure the correct lwjgl libraries are downloaded in the compilation process
    - Compile and run the project with `./run.sh`

## Primary Features
- Physics Sim
    - Environment 
        - Gravity
            - Body Gravity
                - No directional gravity all gravity is from objects
            - Directional Gravity 
                - Base is earth Gravity so going down at 9.8m/s^2
                - Able to change direction and force
        - Time (Simulation speed)
        - Delete if off screen
    - Object
        - Mass
        - Size
        - Elasticity
        - Initial velocity
        - Stating position
        - Color

## UML Diagram
![UML Diagram](demo/diagram/uml-diagram.png?raw=true&v=9e0060f)

## Main Ideas brainstormed so far:
- Physics simulator
	- Balls bouncing around in another ball
	- N-body gravitational simulation
	- SPH fluid simulation 
- Chatroom
- 2-player game
- Scheduling app
- Calendar + to do list + financing + time stamp + hr tracker + sleep tracker 
- Sudoku solver

## Secondary Ideas
- Minecraft mod
- Weight room app
- Recipes app
