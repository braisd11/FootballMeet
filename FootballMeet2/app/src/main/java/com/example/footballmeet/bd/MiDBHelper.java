package com.example.footballmeet.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MiDBHelper extends SQLiteOpenHelper {
    private static final String NOMBRE_BD = "footballmeet.db";
    private static final int VERSION_BD = 1;

    // Tabla Usuarios
    private static final String CREATE_TABLE_USERS = "CREATE TABLE users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "usuario TEXT UNIQUE," +
            "email TEXT," +
            "password TEXT," +
            "nombre_usuario TEXT," +
            "fecha_nacimiento TEXT," +
            "telefono TEXT)";

    // Tabla Partidos Informales
    private static final String CREATE_TABLE_INFORMAL_MATCHES = "CREATE TABLE partidos_informales (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "usuario_id INTEGER," + // La clave foránea para la relación con la tabla de usuarios
            "fecha TEXT," +
            "hora TEXT," +
            "ubicacion TEXT," +
            "descripcion TEXT," +
            "capacidad_jugadores INTEGER," +
            "precio FLOAT," +
            "FOREIGN KEY(usuario_id) REFERENCES users(id))";

    // Tabla Equipos
    private static final String CREATE_TABLE_TEAMS = "CREATE TABLE equipos (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nombre TEXT," +
            "creador_id INTEGER," + // La clave foránea para la relación con la tabla de usuarios
            "descripcion TEXT," +
            "fecha_creacion TEXT," +
            "FOREIGN KEY(creador_id) REFERENCES users(id))";

    // Tabla Torneos
    private static final String CREATE_TABLE_TOURNAMENTS = "CREATE TABLE torneos (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nombre TEXT," +
            "organizador_id INTEGER," + // La clave foránea para la relación con la tabla de usuarios
            "fecha_inicio TEXT," +
            "fecha_fin TEXT," +
            "descripcion TEXT," +
            "precio TEXT," +
            "premio TEXT," +
            "max_equipos INTEGER," +
            "equipos_inscritos INTEGER)";

    // Tabla Participantes en Torneos
    private static final String CREATE_TABLE_TOURNAMENT_TEAMS = "CREATE TABLE equipos_torneo (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "torneo_id INTEGER," + // La clave foránea para la relación con la tabla de torneos
            "equipo_id INTEGER," + // La clave foránea para la relación con la tabla de equipos
            "FOREIGN KEY(torneo_id) REFERENCES torneos(id)," +
            "FOREIGN KEY(equipo_id) REFERENCES equipos(id))";

    public MiDBHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear las tablas definidas en la estructura de la base de datos
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_INFORMAL_MATCHES);
        db.execSQL(CREATE_TABLE_TEAMS);
        db.execSQL(CREATE_TABLE_TOURNAMENTS);
        db.execSQL(CREATE_TABLE_TOURNAMENT_TEAMS);

        insertarUsuarioAdmin(db);
    }


    /**
     * Inserta un usuario administrador al principio
     * @param db
     */
    private static void insertarUsuarioAdmin(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("usuario", "admin");
        values.put("email", "admin@admin.com");
        values.put("password", "admin");
        values.put("nombre_usuario", "admin");
        values.put("fecha_nacimiento", "00/00/0000");
        values.put("telefono", "000000000");
        db.insert("users", null, values);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Manejar actualizaciones de la base de datos, si es necesario
    }


    /**
     * Método para insertar un usuario en la base de datos
     */
    public long insertarUsuario(String usuario, String email, String password, String nombreUsuario, String fechaNacimiento, String telefono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario", usuario);
        values.put("email", email);
        values.put("password", password);
        values.put("nombre_usuario", nombreUsuario);
        values.put("fecha_nacimiento", fechaNacimiento);
        values.put("telefono", telefono);
        // Insertar el usuario en la tabla y devolver el ID del nuevo registro
        return db.insert("users", null, values);
    }

    /**
     * Comprueba si el usuario y la contraseña son correctos
     * @param usuario
     * @param password
     * @return
     */
    public boolean verificarCredenciales(String usuario, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean credencialesCorrectas = false;

        try {
            // Consulta para verificar si el usuario y la contraseña coinciden
            String query = "SELECT * FROM users WHERE usuario = ? AND password = ?";
            cursor = db.rawQuery(query, new String[]{usuario, password});

            // Comprobar si se encontraron resultados
            if (cursor != null && cursor.getCount() > 0) {
                credencialesCorrectas = true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return credencialesCorrectas;
    }


    /**
     * Comprueba si ya existe un usuario con ese nombre
     * @param usuario
     * @return true si ya existe un usuario
     */
    public boolean verificarUsuario(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean usuarioExiste = false;

        try {
            // Consulta para verificar si el usuario existe en la base de datos
            String query = "SELECT * FROM users WHERE usuario = ?";
            cursor = db.rawQuery(query, new String[]{usuario});

            // Comprobar si se encontraron resultados
            if (cursor != null && cursor.getCount() > 0) {
                usuarioExiste = true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return usuarioExiste;
    }
}
