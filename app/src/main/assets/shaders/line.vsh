uniform lowp mat4 u_MVPMatrix;
attribute lowp vec4 a_Position;
void main() {
	gl_Position = u_MVPMatrix * a_Position;
}