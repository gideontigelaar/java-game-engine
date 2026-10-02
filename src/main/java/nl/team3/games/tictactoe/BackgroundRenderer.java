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

            void main()
            {
                vec2 fragCoord = vUV * iResolution;
                float blockPx = 4.0;
                float GroupSize = 20.0;
                float amp = 20.0;
                float freq = 0.01;
                float speed = 15.0;
                float waveSpeed = 2.0;
            
                vec3 colA = vec3(1, 0.824, 0.851);
                vec3 colB = vec3(1, 0.624, 0.678);
            
                vec3 col2A = vec3(0.1, 0.2, 0.9);
                vec3 col2B = vec3(1.0, 0.4, 0.2);
            
            
                vec2 b = floor(fragCoord / blockPx);
            
                float wave = sin((b.x + 0.5) * blockPx * freq + iTime * waveSpeed) * amp;
                b.y += floor(wave / blockPx + 0.5);
            
                vec2 cell = b + floor(iTime * speed);
            
                vec2 group = floor(cell / GroupSize);
                vec2 local = mod(cell, GroupSize);
                if( mod(group.x + group.y, 2.0) == 0.0)
                {
                    if ( local.x > 1.0 && local.x < 18.0 && local.y > 1.0 && local.y < 18.0 )
                    {
                        fragColor = vec4(colA, 1.0);
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
                    }
                    else {
                    fragColor = vec4(1, 0.396, 0.486,1.0);
                    }
                }
            
            
            
            }
            """;



    private final ShaderProgram shader;
    private final Mesh quad;
    private float time = 0f;

    public BackgroundRenderer(AssetManager assets) {
        this.shader = new ShaderProgram(VERTEX_SOURCE, FRAGMENT_SOURCE);
        this.quad = assets.getMesh(AssetManager.QUAD_MESH);
    }

    public void update(float dt) {
        time += dt;
    }

    public void render(int width, int height) {
        shader.bind();
        shader.setUniform1f("iTime", time);
        shader.setUniform2f("iResolution", width, height);
        quad.render();
        shader.unbind();
    }
    public void cleanup() {
        shader.cleanup();
    }
}
