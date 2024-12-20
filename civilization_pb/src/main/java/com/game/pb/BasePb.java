// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Base.proto

package com.game.pb;

public final class BasePb {
  private BasePb() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface BaseOrBuilder extends
      com.google.protobuf.GeneratedMessage.
          ExtendableMessageOrBuilder<Base> {

    // required int32 command = 1;
    /**
     * <code>required int32 command = 1;</code>
     */
    boolean hasCommand();
    /**
     * <code>required int32 command = 1;</code>
     */
    int getCommand();

    // optional int32 code = 2;
    /**
     * <code>optional int32 code = 2;</code>
     */
    boolean hasCode();
    /**
     * <code>optional int32 code = 2;</code>
     */
    int getCode();

    // optional int64 index = 3;
    /**
     * <code>optional int64 index = 3;</code>
     */
    boolean hasIndex();
    /**
     * <code>optional int64 index = 3;</code>
     */
    long getIndex();

    // optional int64 param = 4;
    /**
     * <code>optional int64 param = 4;</code>
     */
    boolean hasParam();
    /**
     * <code>optional int64 param = 4;</code>
     */
    long getParam();
  }
  /**
   * Protobuf type {@code Base}
   */
  public static final class Base extends
      com.google.protobuf.GeneratedMessage.ExtendableMessage<
        Base> implements BaseOrBuilder {
    // Use Base.newBuilder() to construct.
    private Base(com.google.protobuf.GeneratedMessage.ExtendableBuilder<com.game.pb.BasePb.Base, ?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Base(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Base defaultInstance;
    public static Base getDefaultInstance() {
      return defaultInstance;
    }

    public Base getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private Base(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              command_ = input.readInt32();
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              code_ = input.readInt32();
              break;
            }
            case 24: {
              bitField0_ |= 0x00000004;
              index_ = input.readInt64();
              break;
            }
            case 32: {
              bitField0_ |= 0x00000008;
              param_ = input.readInt64();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.game.pb.BasePb.internal_static_Base_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.game.pb.BasePb.internal_static_Base_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.game.pb.BasePb.Base.class, com.game.pb.BasePb.Base.Builder.class);
    }

    public static com.google.protobuf.Parser<Base> PARSER =
        new com.google.protobuf.AbstractParser<Base>() {
      public Base parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new Base(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<Base> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // required int32 command = 1;
    public static final int COMMAND_FIELD_NUMBER = 1;
    private int command_;
    /**
     * <code>required int32 command = 1;</code>
     */
    public boolean hasCommand() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required int32 command = 1;</code>
     */
    public int getCommand() {
      return command_;
    }

    // optional int32 code = 2;
    public static final int CODE_FIELD_NUMBER = 2;
    private int code_;
    /**
     * <code>optional int32 code = 2;</code>
     */
    public boolean hasCode() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional int32 code = 2;</code>
     */
    public int getCode() {
      return code_;
    }

    // optional int64 index = 3;
    public static final int INDEX_FIELD_NUMBER = 3;
    private long index_;
    /**
     * <code>optional int64 index = 3;</code>
     */
    public boolean hasIndex() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>optional int64 index = 3;</code>
     */
    public long getIndex() {
      return index_;
    }

    // optional int64 param = 4;
    public static final int PARAM_FIELD_NUMBER = 4;
    private long param_;
    /**
     * <code>optional int64 param = 4;</code>
     */
    public boolean hasParam() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    /**
     * <code>optional int64 param = 4;</code>
     */
    public long getParam() {
      return param_;
    }

    private void initFields() {
      command_ = 0;
      code_ = 0;
      index_ = 0L;
      param_ = 0L;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      if (!hasCommand()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!extensionsAreInitialized()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      com.google.protobuf.GeneratedMessage
        .ExtendableMessage<com.game.pb.BasePb.Base>.ExtensionWriter extensionWriter =
          newExtensionWriter();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeInt32(1, command_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeInt32(2, code_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeInt64(3, index_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeInt64(4, param_);
      }
      extensionWriter.writeUntil(536870912, output);
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, command_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, code_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(3, index_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(4, param_);
      }
      size += extensionsSerializedSize();
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static com.game.pb.BasePb.Base parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.game.pb.BasePb.Base parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.game.pb.BasePb.Base parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.game.pb.BasePb.Base parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.game.pb.BasePb.Base parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static com.game.pb.BasePb.Base parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static com.game.pb.BasePb.Base parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static com.game.pb.BasePb.Base parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static com.game.pb.BasePb.Base parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static com.game.pb.BasePb.Base parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.game.pb.BasePb.Base prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code Base}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.ExtendableBuilder<
          com.game.pb.BasePb.Base, Builder> implements com.game.pb.BasePb.BaseOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.game.pb.BasePb.internal_static_Base_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.game.pb.BasePb.internal_static_Base_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.game.pb.BasePb.Base.class, com.game.pb.BasePb.Base.Builder.class);
      }

      // Construct using com.game.pb.BasePb.Base.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        command_ = 0;
        bitField0_ = (bitField0_ & ~0x00000001);
        code_ = 0;
        bitField0_ = (bitField0_ & ~0x00000002);
        index_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000004);
        param_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000008);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.game.pb.BasePb.internal_static_Base_descriptor;
      }

      public com.game.pb.BasePb.Base getDefaultInstanceForType() {
        return com.game.pb.BasePb.Base.getDefaultInstance();
      }

      public com.game.pb.BasePb.Base build() {
        com.game.pb.BasePb.Base result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.game.pb.BasePb.Base buildPartial() {
        com.game.pb.BasePb.Base result = new com.game.pb.BasePb.Base(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.command_ = command_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.code_ = code_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.index_ = index_;
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000008;
        }
        result.param_ = param_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.game.pb.BasePb.Base) {
          return mergeFrom((com.game.pb.BasePb.Base)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.game.pb.BasePb.Base other) {
        if (other == com.game.pb.BasePb.Base.getDefaultInstance()) return this;
        if (other.hasCommand()) {
          setCommand(other.getCommand());
        }
        if (other.hasCode()) {
          setCode(other.getCode());
        }
        if (other.hasIndex()) {
          setIndex(other.getIndex());
        }
        if (other.hasParam()) {
          setParam(other.getParam());
        }
        this.mergeExtensionFields(other);
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        if (!hasCommand()) {
          
          return false;
        }
        if (!extensionsAreInitialized()) {
          
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.game.pb.BasePb.Base parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.game.pb.BasePb.Base) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // required int32 command = 1;
      private int command_ ;
      /**
       * <code>required int32 command = 1;</code>
       */
      public boolean hasCommand() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required int32 command = 1;</code>
       */
      public int getCommand() {
        return command_;
      }
      /**
       * <code>required int32 command = 1;</code>
       */
      public Builder setCommand(int value) {
        bitField0_ |= 0x00000001;
        command_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required int32 command = 1;</code>
       */
      public Builder clearCommand() {
        bitField0_ = (bitField0_ & ~0x00000001);
        command_ = 0;
        onChanged();
        return this;
      }

      // optional int32 code = 2;
      private int code_ ;
      /**
       * <code>optional int32 code = 2;</code>
       */
      public boolean hasCode() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional int32 code = 2;</code>
       */
      public int getCode() {
        return code_;
      }
      /**
       * <code>optional int32 code = 2;</code>
       */
      public Builder setCode(int value) {
        bitField0_ |= 0x00000002;
        code_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int32 code = 2;</code>
       */
      public Builder clearCode() {
        bitField0_ = (bitField0_ & ~0x00000002);
        code_ = 0;
        onChanged();
        return this;
      }

      // optional int64 index = 3;
      private long index_ ;
      /**
       * <code>optional int64 index = 3;</code>
       */
      public boolean hasIndex() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>optional int64 index = 3;</code>
       */
      public long getIndex() {
        return index_;
      }
      /**
       * <code>optional int64 index = 3;</code>
       */
      public Builder setIndex(long value) {
        bitField0_ |= 0x00000004;
        index_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int64 index = 3;</code>
       */
      public Builder clearIndex() {
        bitField0_ = (bitField0_ & ~0x00000004);
        index_ = 0L;
        onChanged();
        return this;
      }

      // optional int64 param = 4;
      private long param_ ;
      /**
       * <code>optional int64 param = 4;</code>
       */
      public boolean hasParam() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      /**
       * <code>optional int64 param = 4;</code>
       */
      public long getParam() {
        return param_;
      }
      /**
       * <code>optional int64 param = 4;</code>
       */
      public Builder setParam(long value) {
        bitField0_ |= 0x00000008;
        param_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int64 param = 4;</code>
       */
      public Builder clearParam() {
        bitField0_ = (bitField0_ & ~0x00000008);
        param_ = 0L;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:Base)
    }

    static {
      defaultInstance = new Base(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:Base)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_Base_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_Base_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\nBase.proto\"M\n\004Base\022\017\n\007command\030\001 \002(\005\022\014\n" +
      "\004code\030\002 \001(\005\022\r\n\005index\030\003 \001(\003\022\r\n\005param\030\004 \001(" +
      "\003*\010\010d\020\200\200\200\200\002B\025\n\013com.game.pbB\006BasePb"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_Base_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_Base_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_Base_descriptor,
              new java.lang.String[] { "Command", "Code", "Index", "Param", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
