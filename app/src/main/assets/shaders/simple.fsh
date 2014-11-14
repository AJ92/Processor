uniform lowp sampler2D tex_sampler;
varying lowp vec3 texc;
varying lowp float dir;
void main()
{
	//add simple normal based shading
	lowp vec3 color = texture2D(tex_sampler, texc.st).rgb;
   	gl_FragColor = vec4((color*0.6)+(color*dir*0.4),0.7);
}