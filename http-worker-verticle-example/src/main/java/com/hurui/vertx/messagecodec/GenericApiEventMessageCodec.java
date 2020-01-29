package com.hurui.vertx.messagecodec;

import org.springframework.util.SerializationUtils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class GenericApiEventMessageCodec implements MessageCodec<GenericApiEventMessage, GenericApiEventMessage>{

	@Override
	public void encodeToWire(Buffer buffer, GenericApiEventMessage message) {
		byte[] data = SerializationUtils.serialize(message);
		int length = data.length;
		buffer.appendInt(length);
		buffer.appendBytes(data);
	}

	@Override
	public GenericApiEventMessage decodeFromWire(int position, Buffer buffer) {
		int _pos = position;
		int length = buffer.getInt(_pos);
		byte[] data = buffer.getBytes(_pos += 4, _pos += length); //jump to 4 because getInt() == 4bytes
		return (GenericApiEventMessage) SerializationUtils.deserialize(data);
	}

	@Override
	public GenericApiEventMessage transform(GenericApiEventMessage message) {
		return message;
	}

	@Override
	public String name() {
		return this.getClass().getSimpleName();
	}

	@Override
	public byte systemCodecID() {
		// Always return -1
		return -1;
	}

}
