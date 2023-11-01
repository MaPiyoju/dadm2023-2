const AuthHelper = require('../helper/authHelper');

var rooms = []

module.exports = (socketIO) => {
    socketIO.on('connection', (socket) => {
        
        socket.sessionID = AuthHelper.makeId(10);
        socket.userID = AuthHelper.makeId(10);
        
        console.log(`User ${socket.sessionID} connected`);

        socket.emit("session",{
            sessionID: socket.sessionID,
            rooms: rooms
        })

        socket.on("create_room", (data) => {
            let roomTmp = {
                room: data,
                user1: data
            }
            rooms.push(roomTmp)
            socket.roomId = roomTmp.room;
            socket.join(roomTmp.room);
            socket.broadcast.emit("load_rooms", {rooms: rooms})
        });

        socket.on("join_room", (data) => {
            const room = rooms.find(x=>x.user1 === data.room);
            
            room.user2 = data.user;
            socket.roomId = room.room;
            socket.join(room.room);

            socket.to(room.room).emit("match_found", room.room);
            socket.emit("match_found", room.room)
            
            const ran = Math.floor(Math.random() * 2);
            setTimeout(()=>{
                if(ran == 0)
                    socket.emit("first_turn")
                else
                    socket.broadcast.emit("first_turn")
            },5000)
        });

        socket.on("leave_room", (data) => {
            socket.to(data).emit("empty_room")
            socket.leave(data);

            rooms = rooms.filter(x=>x.room!==data)
            socket.broadcast.emit("load_rooms", {rooms: rooms})
            
            setTimeout(()=>{
                socket.emit("load_rooms", {rooms: rooms})
            },2000)
        });

        socket.on("clear_room", (data) => {
            socket.leave(data);
        });
        
        socket.on("play_turn", (data) => {
            socket.broadcast.to(data.room).emit("start_turn", data.data)
        });

        socket.on("wait_new", (data) => {
            const room = rooms.find(x=>x.user1 === data);
            
            room.wait = room.wait ? room.wait + 1 : 1;

            if(room.wait == 2){
                room.wait = 0
                const ran = Math.floor(Math.random() * 2);
                if(ran == 0){
                    socket.emit("first_turn")
                    socket.broadcast.to(room.room).emit("clear_turn")
                }else{
                    socket.emit("clear_turn")
                    socket.broadcast.to(room.room).emit("first_turn")
                }
            }
        });

        socket.on("disconnecting", () => {
            Array.from(socket.rooms).map(async (room)=>{
                const sockets = await socketIO.in(room).fetchSockets();
                sockets.forEach(s => {
                    s.broadcast.emit("empty_room")
                    rooms = rooms.filter(x=>x.room!==room)
                    socket.broadcast.emit("load_rooms", {rooms: rooms})
                    
                    s.leave(room);
                });
            });
        });

        socket.on("disconnect", () => {
            console.log("user disconnected");
        });
    });
}