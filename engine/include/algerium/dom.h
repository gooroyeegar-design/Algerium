#pragma once
#include <string>
#include <vector>
#include <unordered_map>
#include <memory>
namespace algerium {
struct Node {
    enum class Type { Document, Element, Text };
    Type type=Type::Element;
    std::string name;
    std::string text;
    std::unordered_map<std::string,std::string> attrs;
    std::vector<std::unique_ptr<Node>> children;
    Node* parent=nullptr;
    explicit Node(Type t=Type::Element,std::string n={}):type(t),name(std::move(n)){}
    Node* append(std::unique_ptr<Node> n){n->parent=this; Node* p=n.get(); children.push_back(std::move(n)); return p;}
    std::string attr(const std::string& k) const {auto i=attrs.find(k);return i==attrs.end()?"":i->second;}
    bool has(const std::string& k) const {return attrs.find(k)!=attrs.end();}
};
struct Document {
    std::unique_ptr<Node> root;
    std::string url;
    std::string title;
    Document():root(std::make_unique<Node>(Node::Type::Document,"#document")){}
};
std::vector<Node*> descendants(Node* root);
std::vector<Node*> query_selector_all(Node* root,const std::string& selector);
}
