//{% raw %}
import React, {useState} from 'react';


let nextId = 0;

function FCom(props) {
    const [name, setName] = useState('');
    const [artists, setArtists] = useState([]);

    return (
        <>
            <input
                value={name}
                onChange={e => setName(e.target.value)}
            />

            {/*添加*/}
            <button onClick={() => {
                setArtists([
                    ...artists,
                    {id: nextId++, name: name}
                ]);

                setName("")
            }}>添加
            </button>

            <ul>
                {artists.map(artist => (
                    <li key={artist.id}>
                        {artist.name}
                        {/*删除*/}
                        <button onClick={() => {
                            setArtists(
                                artists.filter(a =>
                                    a.id !== artist.id
                                )
                            );
                        }}>
                            删除
                        </button>
                    </li>
                ))}
            </ul>
        </>
    );
}

export default FCom;
//{% endraw %}
