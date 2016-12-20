#ifdef GL_ES                                                                      
precision mediump float;
#endif                                                                            

varying vec4 v_fragmentColor;                                                     
varying vec2 v_texCoord;                                                          
uniform sampler2D CC_Texture0;

uniform vec2 pixelSize;
uniform int radius;
uniform float weights[64];
uniform vec2 direction;

void main() {
    gl_FragColor = texture2D(CC_Texture0, v_texCoord)*weights[0];
    for (int i = 1; i < radius; i++) {
        vec2 offset = vec2(float(i)*pixelSize.x*direction.x, float(i)*pixelSize.y*direction.y);
        gl_FragColor += texture2D(CC_Texture0, v_texCoord + offset)*weights[i];
        gl_FragColor += texture2D(CC_Texture0, v_texCoord - offset)*weights[i];
    }
} 