# N-Body Physics Simulation

## Presentation Date
May 22, 2025

## Authors
- Aidan Baros
- Charlie Yonkura
- Ethan Zhang

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

## Texture Specification
Textures for bodies should be square in size. For the most accurate texture-hitbox matching, the pixels that constitute the circle should reach the edge of the image.

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

### Example

```json
{
  "simulationName": "Stable Two-Body Orbit",
  "gravityType": "game",
  "bodies": [
    {
      "mass": 1.0,
      "size": 0.07,
      "position": [-0.25, 0.0],
      "velocity": [0.0, 1.0],
      "color": [1.0, 0.0, 0.0],
      "fixed": false
    },
    {
      "mass": 1.0,
      "size": 0.07,
      "position": [0.25, 0.0],
      "velocity": [0.0, -1.0],
      "color": [0.0, 1.0, 0.0],
      "fixed": false
    }
  ]
}
```

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

## UML Diagram
![UML Diagram](demo/diagram/uml-diagram.png?raw=true&v=b8cc536)

