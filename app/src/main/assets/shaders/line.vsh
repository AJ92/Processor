uniform lowp mat4 u_MVPMatrix;
attribute lowp vec4 a_Position;
uniform lowp vec4 u_Color;
varying lowp vec4 fragColor;

void main() {
	fragColor = u_Color;
	gl_Position = u_MVPMatrix * a_Position;
}