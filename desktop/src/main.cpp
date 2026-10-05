#include <SDL.h>
#include <SDL_ttf.h>
#include <curl/curl.h>
#include "algerium/engine.h"
#include "algerium/cookies.h"
#include <string>\n#include <vector>
#include <algorithm>
#include <cctype>\n#include <ctime>
struct NetState { std::vector<std::string> set_cookies; };\nstatic size_t write_cb(char* p,size_t s,size_t n,void* u){auto*out=(std::string*)u;size_t z=s*n;if(out->size()+z>8*1024*1024)return 0;out->append(p,z);return z;}\nstatic size_t header_cb(char* p,size_t s,size_t n,void* u){auto*st=(NetState*)u;size_t z=s*n;std::string h(p,z);if(h.rfind("Set-Cookie:",0)==0||h.rfind("set-cookie:",0)==0)st->set_cookies.push_back(h.substr(h.find(":")+1));return z;}\nstatic bool fetch_page(const std::string& url,std::string& out,CookieJar& jar,long long now){
 CURL* c=curl_easy_init();if(!c)return false;
 curl_easy_setopt(c,CURLOPT_URL,url.c_str());curl_easy_setopt(c,CURLOPT_FOLLOWLOCATION,1L);curl_easy_setopt(c,CURLOPT_MAXREDIRS,8L);
 NetState st; curl_easy_setopt(c,CURLOPT_WRITEFUNCTION,write_cb);curl_easy_setopt(c,CURLOPT_WRITEDATA,&out);curl_easy_setopt(c,CURLOPT_HEADERFUNCTION,header_cb);curl_easy_setopt(c,CURLOPT_HEADERDATA,&st);std::string cookies=jar.cookie_header(url,now);if(!cookies.empty())curl_easy_setopt(c,CURLOPT_COOKIE,cookies.c_str());curl_easy_setopt(c,CURLOPT_USERAGENT,"Algerium/0.3");
 curl_easy_setopt(c,CURLOPT_PROTOCOLS_STR,"http,https");curl_easy_setopt(c,CURLOPT_REDIR_PROTOCOLS_STR,"http,https");curl_easy_setopt(c,CURLOPT_TIMEOUT,20L);curl_easy_setopt(c,CURLOPT_CONNECTTIMEOUT,8L);curl_easy_setopt(c,CURLOPT_NOSIGNAL,1L);curl_easy_setopt(c,CURLOPT_SSL_VERIFYPEER,1L);curl_easy_setopt(c,CURLOPT_SSL_VERIFYHOST,2L);
 CURLcode rc=curl_easy_perform(c); if(rc==CURLE_OK)for(const auto&sc:st.set_cookies)jar.set_cookie(sc,url,now); curl_easy_cleanup(c); return rc==CURLE_OK;
}
static std::string normalize(std::string s){
 s.erase(s.begin(),std::find_if(s.begin(),s.end(),[](unsigned char c){return !std::isspace(c);}));
 s.erase(std::find_if(s.rbegin(),s.rend(),[](unsigned char c){return !std::isspace(c);}).base(),s.end());
 if(s.rfind("http://",0)==0||s.rfind("https://",0)==0)return s;
 if(s.find('.')!=std::string::npos)return "https://"+s;
 std::string q;for(char c:s){if(c==' ')q+='+';else q+=c;}return "https://www.google.com/search?q="+q;
}
static void draw_text(SDL_Renderer*r,TTF_Font*f,const std::string&s,int x,int y,SDL_Color c){
 SDL_Surface*a=TTF_RenderUTF8_Blended(f,s.c_str(),c);if(!a)return;SDL_Texture*t=SDL_CreateTextureFromSurface(r,a);SDL_Rect d{x,y,a->w,a->h};SDL_RenderCopy(r,t,nullptr,&d);SDL_DestroyTexture(t);SDL_FreeSurface(a);
}
int main(){
 if(SDL_Init(SDL_INIT_VIDEO)!=0||TTF_Init()!=0)return 1;curl_global_init(CURL_GLOBAL_DEFAULT);
 SDL_Window*w=SDL_CreateWindow("Algerium",SDL_WINDOWPOS_CENTERED,SDL_WINDOWPOS_CENTERED,1100,760,SDL_WINDOW_RESIZABLE);
 SDL_Renderer*r=SDL_CreateRenderer(w,-1,SDL_RENDERER_ACCELERATED|SDL_RENDERER_PRESENTVSYNC);
 #if defined(_WIN32)
 const char*fontPath="C:/Windows/Fonts/segoeui.ttf";
 #elif defined(__APPLE__)
 const char*fontPath="/System/Library/Fonts/Supplemental/Arial.ttf";
 #else
 const char*fontPath="/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf";
 #endif
 TTF_Font*f=TTF_OpenFont(fontPath,18);if(!f)return 2;
 std::string address="https://example.com",html;algerium::Page page;algerium::CookieJar cookies;bool editing=false;float scroll=0;
 auto load=[&](){address=normalize(address);html.clear();scroll=0;if(fetch_page(address,html,cookies,(long long)std::time(nullptr)))page=algerium::load_document(html,address,1060);};
 load();SDL_StartTextInput();bool run=true;
 while(run){SDL_Event e;while(SDL_PollEvent(&e)){if(e.type==SDL_QUIT)run=false;
  else if(e.type==SDL_MOUSEBUTTONDOWN)editing=e.button.y<58;
  else if(e.type==SDL_TEXTINPUT&&editing)address+=e.text.text;
  else if(e.type==SDL_KEYDOWN){if(editing&&e.key.keysym.sym==SDLK_BACKSPACE&&!address.empty())address.pop_back();if(editing&&e.key.keysym.sym==SDLK_RETURN){editing=false;load();}if(!editing&&e.key.keysym.sym==SDLK_F5)load();if(e.key.keysym.sym==SDLK_DOWN)scroll+=40;if(e.key.keysym.sym==SDLK_UP)scroll=std::max(0.f,scroll-40);}
  else if(e.type==SDL_MOUSEWHEEL)scroll=std::max(0.f,scroll-e.wheel.y*40);}
  SDL_SetRenderDrawColor(r,243,232,210,255);SDL_RenderClear(r);
  SDL_SetRenderDrawColor(r,255,249,238,255);SDL_Rect bar{14,12,1072,42};SDL_RenderFillRect(r,&bar);draw_text(r,f,"Algerium",28,22,{49,88,58,255});draw_text(r,f,address,150,22,{70,60,45,255});
  SDL_SetRenderDrawColor(r,255,255,255,255);SDL_Rect body{14,68,1072,678};SDL_RenderFillRect(r,&body);
  int y=92-(int)scroll;for(const auto&b:page.boxes){if(!b.node||b.node->type!=algerium::Node::Type::Element)continue;std::string value;for(auto&c:b.node->children)if(c->type==algerium::Node::Type::Text)value+=c->text;if(value.empty())continue;std::replace(value.begin(),value.end(),'\n',' ');if(y>760)break;draw_text(r,f,value,24,y,{25,25,28,255});y+=std::max(24.f,b.height)+8;}
  SDL_RenderPresent(r);
 }
 SDL_StopTextInput();TTF_CloseFont(f);SDL_DestroyRenderer(r);SDL_DestroyWindow(w);curl_global_cleanup();TTF_Quit();SDL_Quit();return 0;
}
