#!/usr/bin/env python3
"""
Generate Python gRPC code from proto files.
Usage: python generate_python_proto.py
"""

import subprocess
import sys
from pathlib import Path

def main():
    proto_dir = Path("pborsa-trading/src/main/proto")
    proto_file = proto_dir / "trading_engine.proto"
    output_dir = Path(".")

    if not proto_file.exists():
        print(f"Error: Proto file not found: {proto_file}")
        sys.exit(1)

    print(f"Generating Python gRPC code from {proto_file}...")

    try:
        subprocess.run([
            sys.executable, "-m", "grpc_tools.protoc",
            "-I", str(proto_dir),
            "--python_out", str(output_dir),
            "--grpc_python_out", str(output_dir),
            str(proto_file)
        ], check=True)

        print("[SUCCESS] Generated trading_engine_pb2.py")
        print("[SUCCESS] Generated trading_engine_pb2_grpc.py")
        print("\nPython proto files updated successfully!")
        
    except subprocess.CalledProcessError as e:
        print(f"\nError: Failed to generate Python proto files")
        print("Make sure you have grpcio-tools installed:")
        print("  pip install grpcio-tools")
        sys.exit(1)
    except FileNotFoundError:
        print("\nError: grpc_tools.protoc not found")
        print("Install it with: pip install grpcio-tools")
        sys.exit(1)

if __name__ == "__main__":
    main()

