#!/bin/bash

# Thiết lập biến môi trường (nếu terminal chưa load ~/.bashrc)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin

# 1. Bật máy ảo ở chế độ nền ngầm
echo "Khởi động máy ảo Pixel_4..."
# Kiểm tra xem máy ảo đã chạy chưa
if ! pgrep -f "emulator.*Pixel_4" > /dev/null; then
    emulator -avd Pixel_4 -no-snapshot-load &
    echo "Đang chờ thiết bị Android khởi động..."
    adb wait-for-device
    while [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do
        sleep 2
    done
    echo "Máy ảo đã khởi động xong!"
else
    echo "Máy ảo đã đang chạy."
fi

# 2. Build và cài đặt app
echo "Bắt đầu build và cài đặt ứng dụng (installDebug)..."
./gradlew installDebug

echo "Hoàn tất!"
