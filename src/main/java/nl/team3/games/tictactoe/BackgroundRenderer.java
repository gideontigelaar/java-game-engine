package nl.team3.games.tictactoe;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.graphics.Mesh;
import nl.team3.engine.graphics.ShaderProgram;

public class BackgroundRenderer {

    private static final String VERTEX_SOURCE = """
            #version 330 core

            layout (location = 0) in vec2 aPos;
            layout (location = 1) in vec2 aUV;

            out vec2 vUV;

            void main() {
                vUV = aUV;
                gl_Position = vec4(aPos * 2.0, 0.0, 1.0);
            }
            """;

    private static final String FRAGMENT_SOURCE = """
            #version 330 core

            in vec2 vUV;
            out vec4 fragColor;

            uniform float iTime;
            uniform vec2 iResolution;
            uniform bool transitionToBlue;
            uniform bool lastTransitionToBlue;
            
            
            void main()
            {
                    vec2 fragCoord = vec2(vUV.x * iResolution.x, vUV.y * iResolution.y);
                    float blockPx = 4.0;
                    float GroupSize = 20.0;
                    float amp = 10.0;
                    float freq = 0.01;
                    float speed = 15.0;
                    float waveSpeed = 2.0;
            
                    float circleSpeed = 15.0;
            
            
            
            
            
                    vec3 colA = vec3(1, 0.824, 0.851);
                    vec3 colB = vec3(1, 0.624, 0.678);
                    vec3 colC = vec3(1, 0.396, 0.486);
                    vec3 col2A = vec3(0.502,1.000,0.651);
                    vec3 col2B = vec3(0.459,1.000,0.502);
                    vec3 col2C = vec3(0.000,0.957,0.122);
            
                    vec2 transitionPosition = iResolution.xy / 2.0;
            
                    float startTime = 0.0;
            
                    if (lastTransitionToBlue != transitionToBlue) {
                        startTime = iTime;
                    }
            
                    float expspeed = exp((iTime - startTime) * circleSpeed);
                    float changeValue = 10.0;
            
                    vec2 b = floor(fragCoord / blockPx);
            
                    float wave = sin((b.x + 0.5) * blockPx * freq + iTime * waveSpeed) * amp;
                    //float waveX = sin((b.y + 0.5) * blockPx * freq + iTime * waveSpeed) * amp;
                    b.y += floor(wave / blockPx + 0.5);
                    //b.x += floor(waveX / blockPx + 0.5);
            
                    vec2 cell = b + floor(iTime * speed);
            
                    vec2 snapped = (floor(fragCoord / (blockPx*2.0)) + 0.5) * (blockPx*2.0);
            
            
            
            
                    vec2 group = floor(cell / GroupSize);
                    vec2 local = mod(cell, GroupSize);
                    if( mod(group.x + group.y, 2.0) == 0.0)
                    {
                        if ( local.x > 1.0 && local.x < 18.0 && local.y > 1.0 && local.y < 18.0 )
                        {
                            fragColor = vec4(colA, 1.0);
                            //blue transition
                            if (distance(snapped, transitionPosition) < (expspeed) && transitionToBlue) {fragColor = vec4(col2A, 1.0);}
                            //red transition
                            if (distance(snapped, transitionPosition) < (expspeed) && !transitionToBlue) {fragColor = vec4(colA, 1.0);}
            
                        }
                        else {
                        fragColor = vec4(1.0,1.0,1.0,1.0);
                        }
            
            
                    }
                    else
                    {
                        if ( local.x > 1.0 && local.x < 18.0 && local.y > 1.0 && local.y < 18.0 )
                        {
                            fragColor = vec4(colB, 1.0);
                            //blue transition
                            if (distance(snapped, transitionPosition) < (expspeed) && transitionToBlue) {fragColor = vec4(col2B, 1.0);}
                            //red transition
                            if (distance(snapped, transitionPosition) < (expspeed) && !transitionToBlue) {fragColor = vec4(colB, 1.0);}
            
                        }
                        else {
                        fragColor = vec4(colC,1.0);
            
                        //blue transition
                        if (distance(snapped, transitionPosition) < (expspeed) && transitionToBlue) {fragColor = vec4(col2C,1.0);}
                        //red transition
                        if (distance(snapped, transitionPosition) < (expspeed) && !transitionToBlue) {fragColor = vec4(colC, 1.0);}
            
                        }
                    }
            }
            """;



    private final ShaderProgram shader;
    private final Mesh quad;
    private float time = 0f;
    private boolean blue = true;
    private boolean lastblue = true;


    public BackgroundRenderer(AssetManager assets) {
        this.shader = new ShaderProgram(VERTEX_SOURCE, FRAGMENT_SOURCE);
        this.quad = assets.getMesh(AssetManager.QUAD_MESH);
    }

    public void update(float dt, boolean blue, boolean lastBlue) {
        time += dt;
        this.blue = blue;
        this.lastblue = lastBlue;
    }

    public void render(int width, int height) {
        shader.bind();
        shader.setUniform1f("iTime", time);
        shader.setUniform1b("transitionToBlue", blue);
        shader.setUniform1b("lastTransitionToBlue", lastblue);
        shader.setUniform2f("iResolution", width, height);
        quad.render();
        shader.unbind();
    }
    public void cleanup() {
        shader.cleanup();
    }
}
