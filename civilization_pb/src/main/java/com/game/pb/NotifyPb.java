// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Notify.proto

package com.game.pb;

public final class NotifyPb {
  private NotifyPb() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\014Notify.proto\032\nBase.proto\032\014Common.proto" +
      "B\027\n\013com.game.pbB\010NotifyPb"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.game.pb.BasePb.getDescriptor(),
          com.game.pb.CommonPb.getDescriptor(),
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}