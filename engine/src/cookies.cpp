#include "algerium/cookies.h"
#include "algerium/security.h"
#include <algorithm>
#include <cctype>
#include <sstream>
namespace algerium {
static std::string lower(std::string s){for(char&c:s)c=(char)std::tolower((unsigned char)c);return s;}
static std::string trim(std::string s){auto a=s.find_first_not_of(" \t"); size_t b=s.find_last_not_of(" \t");return a==std::string::npos?"":s.substr(a,b-a+1);}
static bool domain_match(const std::string&host,const std::string&domain){return host==domain||(host.size()>domain.size()&&host.compare(host.size()-domain.size(),domain.size(),domain)==0&&host[host.size()-domain.size()-1]=='.');}
static std::string default_path(const std::string&url){auto p=url.find("://");auto slash=url.find('/',p==std::string::npos?0:p+3);if(slash==std::string::npos)return "/";auto end=url.find_last_of('/');return end==0?"/":url.substr(slash,end-slash);}
bool CookieJar::set_cookie(const std::string&header,const std::string&request_url,long long now){
 auto o=parse_origin(request_url);if(o.opaque)return false;size_t slash=request_url.find('/',request_url.find("://")+3);std::string host=o.host,path=default_path(request_url);
 std::stringstream ss(header);std::string part;std::vector<std::string> parts;while(std::getline(ss,part,';'))parts.push_back(trim(part));if(parts.empty())return false;
 auto eq=parts[0].find('=');if(eq==std::string::npos||eq==0)return false;Cookie c;c.name=parts[0].substr(0,eq);c.value=parts[0].substr(eq+1);c.domain=host;c.path=path;
 for(size_t i=1;i<parts.size();++i){auto x=parts[i]; auto e=x.find('=');std::string k=lower(trim(e==std::string::npos?x:x.substr(0,e))),v=e==std::string::npos?"":trim(x.substr(e+1));if(k=="secure")c.secure=true;else if(k=="httponly")c.http_only=true;else if(k=="path"&&!v.empty()&&v[0]=='/')c.path=v;else if(k=="domain"&&!v.empty()){if(v[0]=='.')v.erase(0,1);v=lower(v);if(!domain_match(host,v))return false;c.domain=v;}else if(k=="max-age"){try{long long n=std::stoll(v);c.expires_at=n<=0?0:now+n;}catch(...){}}}
 if(c.expires_at==0){cookies_.erase(std::remove_if(cookies_.begin(),cookies_.end(),[&](const Cookie&x){return x.name==c.name&&x.domain==c.domain&&x.path==c.path;}),cookies_.end());return true;}
 cookies_.erase(std::remove_if(cookies_.begin(),cookies_.end(),[&](const Cookie&x){return x.name==c.name&&x.domain==c.domain&&x.path==c.path;}),cookies_.end());cookies_.push_back(c);return true;
}
std::string CookieJar::cookie_header(const std::string&url,long long now) const{
 auto o=parse_origin(url);if(o.opaque)return "";std::string path="/";auto p=url.find("://");auto slash=url.find('/',p==std::string::npos?0:p+3);if(slash!=std::string::npos)path=url.substr(slash);
 std::string out;for(const auto&c:cookies_){if(c.expires_at>=0&&c.expires_at<=now)continue;if(!domain_match(o.host,c.domain))continue;if(path.rfind(c.path,0)!=0)continue;if(c.secure&&o.scheme!="https")continue;if(!out.empty())out+="; ";out+=c.name+"="+c.value;}return out;
}
void CookieJar::clear(){cookies_.clear();}
}