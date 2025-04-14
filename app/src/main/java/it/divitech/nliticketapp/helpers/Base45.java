package it.divitech.nliticketapp.helpers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Base45
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static byte[] encode( byte[] src )
    {
        int wholeChunkCount = src.length / ChunkSize;
        byte[] result = new byte[ wholeChunkCount * EncodedChunkSize + ( src.length % ChunkSize == 1 ? SmallEncodedChunkSize : 0 ) ];

        int resultIndex = 0;
        int wholeChunkLength = wholeChunkCount * ChunkSize;

        for( int i = 0; i < wholeChunkLength; )
        {
            int value = ( src[ i++ ] & 0xff ) * ByteSize + ( src[ i++ ] & 0xff );
            result[ resultIndex++ ] = toBase45[ value % BaseSize ];
            result[ resultIndex++ ] = toBase45[ ( value / BaseSize ) % BaseSize ];
            result[ resultIndex++ ] = toBase45[ ( value / ( BaseSize * BaseSize ) ) % BaseSize ];
        }

        if( src.length % ChunkSize != 0 )
        {
            result[ result.length - 2 ] = toBase45[ ( src[ src.length - 1 ] & 0xff ) % BaseSize ];
            result[ result.length - 1 ] = ( src[ src.length - 1 ] & 0xff ) < BaseSize ? toBase45[ 0 ] : toBase45[ ( src[ src.length - 1 ] & 0xff ) / BaseSize % BaseSize ];
        }
        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static byte[] decode( byte[] src )
    {
        int remainderSize = src.length % EncodedChunkSize;
        int[] buffer = new int[ src.length ];

        for( int i = 0; i < src.length; ++i )
        {
            buffer[ i ] = fromBase45[ Byte.toUnsignedInt( src[ i ] ) ];

            if( buffer[ i ] == -1 )
                throw new IllegalArgumentException();

        }

        int wholeChunkCount = buffer.length / EncodedChunkSize;
        byte[] result = new byte[ wholeChunkCount * ChunkSize + ( remainderSize == ChunkSize ? 1 : 0 ) ];
        int resultIndex = 0;
        int wholeChunkLength = wholeChunkCount * EncodedChunkSize;

        for( int i = 0; i < wholeChunkLength; )
        {
            int val = buffer[ i++ ] + BaseSize * buffer[ i++ ] + BaseSize * BaseSize * buffer[ i++ ];

            if( val > 0xFFFF )
                throw new IllegalArgumentException();

            result[ resultIndex++ ] = (byte)( val / ByteSize );
            result[ resultIndex++ ] = (byte)( val % ByteSize );
        }

        if( remainderSize != 0 )
            result[ resultIndex ] = (byte)( buffer[ buffer.length - 2 ] + BaseSize * buffer[ buffer.length - 1 ] );

        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private static final int BaseSize = 45;
    private static final int ChunkSize = 2;
    private static final int EncodedChunkSize = 3;
    private static final int SmallEncodedChunkSize = 2;
    private static final int ByteSize = 256;

    private static final byte[] toBase45 =
    {  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
       'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
       'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '$', '%',
       '*', '+', '-', '.', '/', ':'
    };

    private static final int[] fromBase45 = new int[ 256 ];

    static
    {
        Arrays.fill( fromBase45, -1 );

        for( int i = 0; i < toBase45.length; i++ )
        {
            fromBase45[ toBase45[ i ] ] = i;
        }
    }

}
