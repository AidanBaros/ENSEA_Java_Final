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

## JSON Formatting Specification
Simulation scenarios are encoded using JSON files. The JSON files are located in demo/scenario/. The scenario file includes simulation parameters and body data.

### JSON Fields
| Field           | Type     | Required | Description                                                                 |
|-----------------|----------|----------|-----------------------------------------------------------------------------|
| simulationName  | string   | No       | Name of the simulation (used as window title).                              |
| gravityType     | string   | No       | Type of gravity to use. Options: `"game"`, `"standard"`, `"earth"`. Defaults to `"game"`. |
| bodies          | array    | Yes      | List of body objects (see below).                                           |

### Body Object Fields

| Field        | Type     | Required | Description                                                                 |
|--------------|----------|----------|-----------------------------------------------------------------------------|
| mass         | number   | Yes      | Mass of the body.                                                           |
| size         | number   | Yes      | Size (radius) of the body.                                                  |
| position     | array    | Yes      | `[x, y]` position of the body (2 numbers).                                  |
| velocity     | array    | Yes      | `[vx, vy]` initial velocity (2 numbers).                                    |
| color        | array    | No       | `[r, g, b]` color (each 0.0–1.0). If omitted, defaults to white or texture. |
| fixed        | boolean  | No       | If true, body does not move. Defaults to `false`.                           |
| texturePath  | string   | No       | Path to a texture image for the body. If omitted, uses color.               |

---

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

## UML Diagram
