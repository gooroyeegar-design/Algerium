#include "algerium/security.h"
#include <algorithm>
#include <cctype>
#include <regex>
namespace algerium {
static std::string lower(std::string s){for(char&c:s)c=(char)std::tolower((unsigned char)c);return s;}
Origin parse_origin(const std::string&url){
 Origin o;auto p=url.find("://");if(p==std::string::npos){o.opaque=true;return o;}
 o.scheme=lower(url.substr(0,p));size_t a=p+3,slash=url.find_first_of("/?#",a);std::string authority=url.substr(a,slash==std::string::npos?url.size()-a:slash-a);
 if(authority.empty()||authority.find('@')!=std::string::npos){o.opaque=true;return o;}
 if(authority[0]=='['){size_t close=authority.find(']');if(close==std::string::npos){o.opaque=true;return o;}o.host=lower(authority.substr(1,close-1));if(close+1<authority.size()){if(authority[close+1]!=':'){o.opaque=true;return o;}o.port=authority.substr(close+2);}}
 else{size_t colon=authority.rfind(':');if(colon!=std::string::npos&&authority.find(':')==colon){o.host=lower(authority.substr(0,colon));o.port=authority.substr(colon+1);}else{o.host=lower(authority);}}
 if(o.host.empty()){o.opaque=true;return o;} if(!o.port.empty()&&!std::all_of(o.port.begin(),o.port.end(),[](unsigned char c){return std::isdigit(c); })){o.opaque=true;return o;} if(!o.port.empty()){try{unsigned long pn=std::stoul(o.port);if(pn>65535){o.opaque=true;return o;}}catch(...){o.opaque=true;return o;}} if(o.port.empty())o.port=(o.scheme=="https"?"443":o.scheme=="http"?"80":"");
 if(o.scheme!="http"&&o.scheme!="https")o.opaque=true;return o;
}
bool same_origin(const Origin&a,const Origin&b){return !a.opaque&&!b.opaque&&a.scheme==b.scheme&&a.host==b.host&&a.port==b.port;}
bool is_private_host(const std::string&raw){
 std::string h=lower(raw);if(h=="localhost"||h=="localhost.localdomain"||h=="::1"||h=="0.0.0.0"||h=="::"||h=="fe80::1")return true;
 std::regex ipv4(R"(^([0-9]+)\.([0-9]+)\.([0-9]+)\.([0-9]+)$)");std::smatch m;
 if(std::regex_match(h,m,ipv4)){int a=std::stoi(m[1]),b=std::stoi(m[2]);return a==10||a==127||(a==172&&b>=16&&b<=31)||(a==192&&b==168)||(a==169&&b==254);}
 if(h.rfind("fc",0)==0||h.rfind("fd",0)==0||h.rfind("fe8",0)==0||h.rfind("fe9",0)==0||h.rfind("fea",0)==0||h.rfind("feb",0)==0)return true;
 return false;
}
bool is_potentially_trustworthy(const std::string&url){auto o=parse_origin(url);return !o.opaque&&(o.scheme=="https"||o.host=="localhost"||o.host=="127.0.0.1"||o.host=="::1");}
bool is_safe_navigation(const std::string&url){auto o=parse_origin(url);if(o.opaque||o.host.empty())return false;if(o.scheme!="http"&&o.scheme!="https")return false;return url.find('\n')==std::string::npos&&url.find('\r')==std::string::npos&&url.find('@')==std::string::npos;}
}