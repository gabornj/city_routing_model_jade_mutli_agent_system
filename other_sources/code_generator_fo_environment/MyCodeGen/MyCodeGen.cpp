//FOR VERTICAL
//Omid55
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
using namespace std;

bool isDigit(char ch)
{
	return '0'<=ch && ch<='9';
}

string myTrim(string str)
{
	int index=0;
	while(index<str.length() && str[index]==' ') 
	{
		index++;
	}
	str=str.substr(index);
	index=str.length()-1;
	while(index<str.length() && str[index]==' ') 
	{
		index--;
	}
	str=str.substr(0,index+1);
	return str;
}

int main()
{
	string str=" o mid  ";
	fstream f("code.java",ios::in);
	fstream g("output.java",ios::out);
	int a[4];
	int b[4];
	while(!f.eof())
	{
		getline(f,str);
		str=myTrim(str);
		if(str.length()<=0) continue;
		stringstream ss(str);
		int index=0;
		char ch;
		while(ss.good())
		{
			string num="";
			ch=ss.get();
			while(ss.good() && !isDigit(ch))
			{
				ch=ss.get();
			}
			if(ss.bad()) break;
			do
			{
				if(!isDigit(ch)) break;
				num+=ch;
				ch=ss.get();
			}
			while(isDigit(ch) && ss.good());
			if(ss.bad()) break;
			a[index++]=atoi(num.c_str());
		}

		getline(f,str);
		stringstream ss2(str);
		index=0;
		while(ss2.good())
		{
			string num="";
			ch=ss2.get();
			while(ss2.good() && !isDigit(ch))
			{
				ch=ss2.get();
			}
			if(ss2.bad()) break;
			do
			{
				num+=ch;
				ch=ss2.get();
			}
			while(isDigit(ch) && ss2.good());
			if(ss2.bad()) break;
			b[index++]=atoi(num.c_str());
		}
		

		int r=a[1]+8;
		int m1=(a[0]+b[0])/2;
		int m2=(a[2]+b[2])/2;
		// inputs =>
		g<<"drawMyLine("<<a[0]<<", "<<a[1]<<", "<<a[2]<<", "<<a[3]<<");\n";
		g<<"drawMyLine("<<b[0]<<", "<<b[1]<<", "<<b[2]<<", "<<b[3]<<");\n";
		// now our outputs
		for(int i=0;i<4;i++,r+=23)
		{
			g<<"drawMyLine("<<m1<<", "<<r<<", "<<m2<<", "<<r+15<<");\n";
		}
		g<<endl;
	}
	return 0;
}