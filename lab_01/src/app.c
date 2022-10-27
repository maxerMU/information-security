#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <sys/ioctl.h>
#include <net/if.h> 
#include <unistd.h>
#include <netinet/in.h>
#include <string.h>

#define BUF_SIZE 1024
#define INVALID_MAC 1

bool mac_adress_check(void)
{
    char buf[BUF_SIZE] = {0};

    struct ifreq ifr;
    struct ifconf ifc;
    int success = 0;

    int sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_IP);
    if (sock == -1) { 
        return false;
     };

    ifc.ifc_len = sizeof(buf);
    ifc.ifc_buf = buf;
    if (ioctl(sock, SIOCGIFCONF, &ifc) == -1) { 
        close(sock);
        return false;
     }

    struct ifreq* it = ifc.ifc_req;
    const struct ifreq* const end = it + (ifc.ifc_len / sizeof(struct ifreq));

    for (; it != end; ++it) {
        strcpy(ifr.ifr_name, it->ifr_name);
        if (ioctl(sock, SIOCGIFFLAGS, &ifr) == 0) {
            if (! (ifr.ifr_flags & IFF_LOOPBACK)) { // don't count loopback
                if (ioctl(sock, SIOCGIFHWADDR, &ifr) == 0) {
                    success = 1;
                    break;
                }
            }
        }
        else { 
            close(sock);
            return false;
         }
    }

    unsigned char mac_address[6 + 1];

    if (success) {
        memcpy(mac_address, ifr.ifr_hwaddr.sa_data, 6);
    }
    else {
        close(sock);
        return false;
    }
    mac_address[6] = '\0';

    return !strcmp(mac_address, MAGIC);
}

int main(void)
{
    if (!mac_adress_check())
    {
        printf("Invalid MAC address!\n");
        return INVALID_MAC;
    }

    printf("Here we go!\n");
    return EXIT_SUCCESS;
}