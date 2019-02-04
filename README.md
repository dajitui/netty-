# netty-
只是实现了2个直播间，然后可以进行文字通讯，图片的话上传保存到本地D:\nginx-1.14.2\html\netty项目照片\image，使用nginx映射。

nginx.conf
location /image {
            root   D:/nginx-1.14.2/html/netty项目照片;
        }
