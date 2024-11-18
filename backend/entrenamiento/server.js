const express = require('express');
const bodyParser = require('body-parser');
const http = require('http');
const socketIo = require('socket.io');  // Importamos socket.io

const app = express();
const port = 3000;

app.use(bodyParser.json()); // Para manejar datos JSON en el body

// Creamos el servidor HTTP usando Express
const server = http.createServer(app);

// Creamos el servidor de WebSocket con Socket.io
const io = socketIo(server);

// Array de música predefinido
let music = [
    { id: 1, title: 'Song 1', artist: 'Artist 1', album: 'Album 1' },
    { id: 2, title: 'Song 2', artist: 'Artist 2', album: 'Album 2' },
    { id: 3, title: 'Song 3', artist: 'Artist 3', album: 'Album 3' }
];

// Conexión de Socket.io
io.on('connection', (socket) => {
    console.log('Cliente conectado');

    // Limpiar el intervalo cuando el cliente se desconecta
    socket.on('disconnect', () => {
        console.log('Cliente desconectado');
    });
});

// Endpoint GET para obtener la lista de música
app.get('/music', (req, res) => {
    // Envia la lista de música a través de WebSockets
    io.emit('updateMusicList', music);  // Envia la lista de música a todos los clientes conectados
    res.json(music);  // También respondemos con la lista por la ruta HTTP
});

// Endpoint POST para agregar una nueva canción
app.post('/music', (req, res) => {
    const newSong = {
        id: music.length + 1,
        title: req.body.title,
        artist: req.body.artist,
        album: req.body.album
    };
    music.push(newSong);

    // Emitimos la lista de música actualizada a todos los clientes conectados
    io.emit('updateMusicList', music);  // Envia la lista de música a todos los clientes

    res.status(201).json(newSong);  // Responde de forma tradicional con la nueva canción
});

// Endpoint PUT para actualizar una canción
app.put('/music/:id', (req, res) => {
    const song = music.find(s => s.id == req.params.id);
    if (!song) return res.status(404).json({ message: 'Canción no encontrada' });

    // Actualizar los campos de la canción
    song.title = req.body.title || song.title;
    song.artist = req.body.artist || song.artist;
    song.album = req.body.album || song.album;

    // Emitimos la lista de música actualizada a todos los clientes conectados
    io.emit('updateMusicList', music);  // Envia la lista de música a todos los clientes

    res.json({ message: 'Canción actualizada', song });  // Responde de forma tradicional con la canción actualizada
});

// Endpoint DELETE para eliminar una canción
app.delete('/music/:id', (req, res) => {
    const index = music.findIndex(s => s.id == req.params.id);
    if (index === -1) return res.status(404).json({ message: 'Canción no encontrada' });

    const deletedSong = music.splice(index, 1);  // Eliminar la canción

    // Emitimos la lista de música actualizada a todos los clientes conectados
    io.emit('updateMusicList', music);  // Envia la lista de música a todos los clientes

    res.json({ message: 'Canción eliminada', song: deletedSong });  // Responde de forma tradicional con la canción eliminada
});

// Iniciar el servidor
server.listen(port, () => {
    console.log(`Servidor escuchando en http://localhost:${port}`);
});