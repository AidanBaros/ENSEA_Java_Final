// package com.ensea_java_final;




// public class PhysicsUtilsTest {


//     private PhysicsUtilsTest() {}

//     public static Body buildCircle(World world, float radius, float density, Vec2 position, float restitution) {

//         BodyDef def = new BodyDef();
//         def.type = BodyType.DYNAMIC;
//         def.position.set(position);

//         Body body = world.createBody(def);

//         org.jbox2d.collision.shapes.CircleShape shape = new CircleShape();

//         shape.setRadius(radius);


//         FixtureDef fx = new FixtureDef();

//         fx.shape = shape;

//         fx.density = density;

//         fx.restitution = restitution;

//         body.createFixture(fx);

//         return body;

//     }


//     public static void assertVertEquals(Vec2 expected, Vector2D actual, double eps) {
//         assertAll(
//             () -> assertEquals(expected.x, actual.x, eps, "x differs"),
//             () -> assertEquals(expected.y, actual.y, eps, "y differs")
//         );
//     }
    
// }
