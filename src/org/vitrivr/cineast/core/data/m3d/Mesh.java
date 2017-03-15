package org.vitrivr.cineast.core.data.m3d;

import com.jogamp.opengl.GL2;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4i;

import java.util.*;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;

/**
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public class Mesh implements Renderable, WritableMesh {

    /** The default, empty mesh. */
    public static final Mesh EMPTY = new Mesh(1,1,1);

    /** A face defined by the normal and the vertex-indices. */
    public class Face {
        private FaceType type;
        private Vector4i vertexIndices;
        private Vector4i normalIndices;

        public final FaceType getType() {
            return type;
        }
        public final Vector4i getVertexIndices() {
            return vertexIndices;
        }
        public final Vector4i getNormalIndices() {
            return normalIndices;
        }

        /**
         *
         * @return
         */
        public final List<Vector3f> getVertices() {
            List<Vector3f> vertices = new ArrayList<>((this.type == FaceType.QUAD) ? 4 : 3);
            vertices.add(Mesh.this.vertices.get(this.vertexIndices.x - 1));
            vertices.add(Mesh.this.vertices.get(this.vertexIndices.y - 1));
            vertices.add(Mesh.this.vertices.get(this.vertexIndices.z - 1));
            if (this.type == FaceType.QUAD) vertices.add(Mesh.this.vertices.get(this.vertexIndices.w - 1));
            return vertices;
        }

        /**
         *
         * @return
         */
        public final List<Vector3f> getColors() {
            List<Vector3f> colors = new ArrayList<>((this.type == FaceType.QUAD) ? 4 : 3);
            colors.add(Mesh.this.colors.get(this.vertexIndices.x - 1));
            colors.add(Mesh.this.colors.get(this.vertexIndices.y - 1));
            colors.add(Mesh.this.colors.get(this.vertexIndices.z - 1));
            if (this.type == FaceType.QUAD) colors.add(Mesh.this.colors.get(this.vertexIndices.w - 1));
            return colors;
        }

        /**
         *
         * @return
         */
        public final List<Vector3f> getNormals() {
            if (this.normalIndices != null) {
                List<Vector3f> normals = new ArrayList<>((this.type == FaceType.QUAD) ? 4 : 3);
                normals.add(Mesh.this.normals.get(this.normalIndices.x - 1));
                normals.add(Mesh.this.normals.get(this.normalIndices.y - 1));
                normals.add(Mesh.this.normals.get(this.normalIndices.z - 1));
                if (this.type == FaceType.QUAD) normals.add(Mesh.this.normals.get(this.normalIndices.w - 1));
                return normals;
            } else {
                return null;
            }
        }
    }



    /** Enumeration used to distinguish between triangular and quadratic faces. */
    public enum FaceType {
        TRI(GL_TRIANGLES),QUAD(GL_QUADS);
        int gl_draw_type;
        FaceType(int type) {
            this.gl_draw_type = type;
        }
    }

    /** List of vertices in the Mesh. */
    private final List<Vector3f> vertices;

    /** List of vertex normals in the Mesh. */
    private final List<Vector3f> normals;

    /** List of faces in the mesh. */
    private final List<Face> faces;

    /**
     * List of color vectors in the mesh. Each vector contains an RGB color and the color indices
     * correspond to the indices of the vertices (i.e. color at positionCamera 1 belongs to vertex at same positionCamera).
     */
    private List<Vector3f> colors = new ArrayList<>();

    /**
     * Copy constructor for Mesh.
     *
     * @param mesh Mesh that should be copied.
     */
    public Mesh(Mesh mesh) {
        this(mesh.numberOfFaces(), mesh.numberOfNormals(), mesh.numberOfVertices());

        for (Vector3f vertex : mesh.vertices) {
            this.vertices.add(new Vector3f(vertex));
        }

        for (Vector3f normal : mesh.normals) {
            this.normals.add(new Vector3f(normal));
        }

        for (Vector3f color : mesh.colors) {
            this.colors.add(new Vector3f(color));
        }

        for (Face face : mesh.faces) {
            Mesh.Face newFace = new Face();
            newFace.type = face.type;
            newFace.vertexIndices = new Vector4i(face.vertexIndices.x, face.vertexIndices.y, face.vertexIndices.z, face.vertexIndices.w);
            if (face.normalIndices != null) newFace.normalIndices = new Vector4i(face.normalIndices.x, face.normalIndices.y, face.normalIndices.z, face.normalIndices.w);
            this.faces.add(newFace);
        }
    }

    /**
     * Default constructor.
     *
     * @param faces Expected number of faces (not a fixed limit).
     * @param normals Expected number of vertex normals (not a fixed limit).
     * @param vertices  Expected number of vertices (not a fixed limit).
     */
    public Mesh(int faces, int normals, int vertices) {
        this.faces = new ArrayList<>(faces);
        this.vertices = new ArrayList<>(normals);
        this.normals = new ArrayList<>(vertices);
        this.colors = new ArrayList<>(vertices);
    }

    /**
     * Adds an vector defining a vertex to the Mesh.
     *
     * @param vertex
     */
    public void addVertex(Vector3f vertex) {
        this.vertices.add(vertex);
        this.colors.add(new Vector3f(1.0f, 1.0f, 1.0f));
    }

    /**
     * Adds an vector defining a vertex to the Mesh.
     *
     * @param vertex
     */
    public void addVertex(Vector3f vertex, Vector3f color) {
        this.vertices.add(vertex);
        this.colors.add(color);
    }

    /**
     *
     * @param color
     */
    public void updateColor(Vector3f color) {
        for (int i = 0;i<this.colors.size();i++) {
            this.colors.set(i, color);
        }
    }

    /**
     * Adds an vector defining a vertex to the Mesh.
     */
    public void updateColor(int vertexIndex, Vector3f color) {
        this.colors.set(vertexIndex, color);
    }

    /**
     * Adds a vector defining a vertex normal to the Mesh.
     *
     * @param normal Vector defining the vertex-normal.
     */
    public void addNormal(Vector3f normal) {
        this.normals.add(normal);
    }

    /**
     * Adds a new triangular face to the Mesh. Faces index vertices and
     * vertex normals.
     *
     * @param vertices Vector3i containing the indices of the vertices.
     * @param normals Vector3i containing the indices of the normal vectors. May be null.
     */
    public void addFace(Vector3i vertices, Vector3i normals) {
        Mesh.Face face = new Face();
        face.type = FaceType.TRI;
        face.vertexIndices = new Vector4i(vertices, 0);
        if (normals!= null) face.normalIndices = new Vector4i(normals, 0);
        this.faces.add(face);
    }

    /**
     * Adds a new quadratic face to the Mesh. Faces index vertices and
     * vertex normals.
     *
     * @param vertices Vector4i containing the indices of the vertices.
     * @param normals Vector4i containing the indices of the normal vectors. May be null.
     */
    public void addFace(Vector4i vertices, Vector4i normals) {
        Mesh.Face face = new Face();
        face.type = FaceType.QUAD;
        face.vertexIndices = vertices;
        face.normalIndices = normals;
        this.faces.add(face);
    }

    /**
     * Returns the list of vertices. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of vertices.
     */
    public List<Vector3f> getVertices() {
        return Collections.unmodifiableList(this.vertices);
    }

    /**
     * Returns the list of vertex-normals. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of vertex normals.
     */
    public List<Vector3f> getNormals() {
        return Collections.unmodifiableList(this.normals);
    }

    /**
     * Returns the list of vertex-normals. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of vertex normals.
     */
    public List<Vector3f> getColors() {
        return Collections.unmodifiableList(this.colors);
    }

    /**
     * Returns the list of vertex-normals. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of faces.
     */
    public List<Face> getFaces() {
        return Collections.unmodifiableList(faces);
    }

    /**
     * Returns the number of normal vectors in this Mesh.
     *
     * @return Number of normal vectors.
     */
    public final int numberOfNormals() {
        return this.normals.size();
    }

    /**
     * Returns the number of vertices in this Mesh.
     *
     * @return Number of vertices.
     */
    public final int numberOfVertices() {
        return this.vertices.size();
    }

    /**
     * Returns the number of faces in this Mesh.
     *
     * @return Number of faces.
     */
    public final int numberOfFaces() {
        return this.faces.size();
    }

    /**
     * Indicates, whether the mesh is an empty Mesh or not
     *
     * @return True if mesh is empty, false otherwise.
     */
    public final boolean isEmpty() {
        return this.faces.isEmpty();
    }

    /**
     * Assembles a mesh into a new glDisplayList. The method returns a handle for this
     * newly created glDisplayList. To actually render the list - by executing the commands it contains -
     * the glCallList function must be called!
     *
     * IMPORTANT: The glDisplayList bust be deleted once its not used anymore by calling glDeleteLists

     * @return Handle for the newly created glDisplayList.
     */
    public int assemble(GL2 gl) {
        int meshList = gl.glGenLists(1);
        gl.glNewList(meshList, GL_COMPILE);
        {
            for (Face face : this.faces) {
                /* Extract normals and vertices. */
                List<Vector3f> vertices = face.getVertices();
                List<Vector3f> colors = face.getColors();
                List<Vector3f> normals = face.getNormals();

                /* Drawing is handled differently depending on whether its a TRI or QUAD mesh. */
                gl.glBegin(face.type.gl_draw_type);
                {
                    gl.glColor3f(colors.get(0).x, colors.get(0).y, colors.get(0).z);
                    gl.glVertex3f(vertices.get(0).x, vertices.get(0).y, vertices.get(0).z);
                    gl.glColor3f(colors.get(1).x, colors.get(1).y, colors.get(1).z);
                    gl.glVertex3f(vertices.get(1).x, vertices.get(1).y, vertices.get(1).z);
                    gl.glColor3f(colors.get(2).x, colors.get(2).y, colors.get(2).z);
                    gl.glVertex3f(vertices.get(2).x, vertices.get(2).y, vertices.get(2).z);
                    if (face.type == FaceType.QUAD) {
                        gl.glColor3f(colors.get(3).x, colors.get(3).y, colors.get(3).z);
                        gl.glVertex3f(vertices.get(3).x, vertices.get(3).y, vertices.get(3).z);
                    }

                    if (normals != null && normals.size() >= 3) {
                        gl.glNormal3f(normals.get(0).x, normals.get(0).y, normals.get(0).z);
                        gl.glNormal3f(normals.get(1).x, normals.get(1).y, normals.get(1).z);
                        gl.glNormal3f(normals.get(2).x, normals.get(2).y, normals.get(2).z);
                        if (face.type == FaceType.QUAD) gl.glNormal3f(normals.get(3).x, normals.get(3).y, normals.get(3).z);
                    }
                }
                gl.glEnd();
            }
        }
        gl.glEndList();
        return meshList;
    }
}
