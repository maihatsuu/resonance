#version 150

uniform sampler2D DepthSampler;
uniform mat4 InvViewProj;
uniform vec3 Center;
uniform float Front;
uniform float Fade;

in vec2 screenUv;

out vec4 fragColor;

const vec3 AMETHYST = vec3(0.72, 0.45, 1.0);
const vec3 EDGE = vec3(0.95, 0.85, 1.0);

void main() {
    float depth = texture(DepthSampler, screenUv).r;
    if (depth >= 1.0) {
        discard; // sky
    }

    // Rebuild this pixel's camera-relative world position from the depth buffer.
    vec4 clip = vec4(screenUv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 world = InvViewProj * clip;
    float behindFront = Front - distance(world.xyz / world.w, Center);

    // Sharp leading edge with a glow trailing behind it.
    float intensity = behindFront < 0.0
            ? smoothstep(-0.6, 0.0, behindFront)
            : exp(-behindFront * 0.7);
    if (intensity <= 0.001) {
        discard;
    }

    fragColor = vec4(mix(AMETHYST, EDGE, intensity * intensity), intensity * Fade * 0.8);
}
